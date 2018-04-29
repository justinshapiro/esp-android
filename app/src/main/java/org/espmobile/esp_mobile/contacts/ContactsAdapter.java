package org.espmobile.esp_mobile.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.Contact;
import org.json.JSONObject;

import java.util.List;

public class ContactsAdapter extends RecyclerSwipeAdapter<ContactsAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageView contactLetter;
        Button deleteButton, editButton;
        SwipeLayout swipeLayout;

        ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            contactLetter = itemView.findViewById(R.id.letter_image_view);
            deleteButton = itemView.findViewById(R.id.delete_contact_button);
            editButton = itemView.findViewById(R.id.edit_contact_button);
            swipeLayout = itemView.findViewById(R.id.item_contact);
        }
    }

    private Context outsideContext;
    private List<Contact> mContacts;

    ContactsAdapter(Context c, List<Contact> contacts) {
        this.outsideContext = c;
        this.mContacts = contacts;
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsAdapter.ViewHolder viewHolder, final int position) {
        Contact contact = mContacts.get(position);
        TextView nameTextView = viewHolder.nameTextView;
        nameTextView.setText(contact.getName());
        TextView phoneTextView = viewHolder.phoneTextView;
        phoneTextView.setText(Contact.formatPhoneNumber(contact.getPhone()));

        String letter = String.valueOf(contact.getName().charAt(0));
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(3)
                .textColor(Color.BLACK)
                .bold()
                .endConfig()
                .buildRound(letter, Color.GRAY);
        viewHolder.contactLetter.setImageDrawable(drawable);

        // Handle the edit button click
        viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog addContact = new Dialog(view.getContext());
                addContact.setContentView(R.layout.popup_edit_contact);
                TextView firstNameField = addContact.findViewById(R.id.edit_first_name);
                TextView lastNameField = addContact.findViewById(R.id.edit_last_name);
                final TextView phoneNumberField = addContact.findViewById(R.id.edit_phone_number);

                String[] contactNameComponents = mContacts.get(position).getName().split(" ");
                if (contactNameComponents.length > 1) {
                    firstNameField.setText(contactNameComponents[0]);
                    lastNameField.setText(contactNameComponents[1]);
                } else {
                    firstNameField.setText(contactNameComponents[0]);
                }

                phoneNumberField.setText(mContacts.get(position).getPhone());

                firstNameField.setEnabled(false);
                firstNameField.setAlpha((float) 0.75);
                lastNameField.setEnabled(false);
                lastNameField.setAlpha((float) 0.75);

                addContact.findViewById(R.id.edit_save_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addContact.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        phoneNumberField.setEnabled(false);
                        phoneNumberField.setAlpha((float) 0.75);
                        addContact.findViewById(R.id.edit_save_button).setEnabled(false);
                        addContact.findViewById(R.id.edit_save_button).setAlpha((float) 0.75);
                        ESPMobileAPI.updateContactPhone(
                                outsideContext,
                                mContacts.get(position).getID(),
                                phoneNumberField.getText().toString(),
                                new ParsedRequestListener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Contact editedContact = new Contact(
                                                mContacts.get(position).getID(),
                                                mContacts.get(position).getName(),
                                                phoneNumberField.getText().toString(),
                                                mContacts.get(position).getGroupID()
                                        );
                                        mContacts.remove(position);
                                        mContacts.add(position, editedContact);
                                        notifyItemChanged(position);
                                        addContact.dismiss();
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        addContact.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        phoneNumberField.setEnabled(true);
                                        phoneNumberField.setAlpha(1);
                                        addContact.findViewById(R.id.edit_save_button).setEnabled(true);
                                        addContact.findViewById(R.id.edit_save_button).setAlpha(1);
                                        MessageBanner.show((Activity) outsideContext, R.string.error_message, R.color.espRed);
                                    }
                                });
                    }
                });

                Window currentWindow = addContact.getWindow();
                if (currentWindow != null) {
                    currentWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                // Close the contact when the dialog is dismissed
                addContact.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mItemManger.closeItem(viewHolder.getAdapterPosition());
                    }
                });

                addContact.show();
            }
        });

        // Handle the delete button click
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EmergencyContactsActivity) outsideContext).renderWaitingState(true);
                ESPMobileAPI.deleteContact(outsideContext, mContacts.get(position).getID(), new ParsedRequestListener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ((EmergencyContactsActivity) outsideContext).renderWaitingState(false);
                        mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                        mContacts.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mContacts.size());
                    }

                    @Override
                    public void onError(ANError anError) {
                        ((EmergencyContactsActivity) outsideContext).renderWaitingState(false);
                        MessageBanner.show((Activity) outsideContext, R.string.error_message, R.color.espRed);
                        mItemManger.closeItem(viewHolder.getAdapterPosition());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.item_contact;
    }
}
