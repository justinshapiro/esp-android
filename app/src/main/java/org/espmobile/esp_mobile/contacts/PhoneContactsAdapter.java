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

import org.espmobile.esp_mobile.Contact;
import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PhoneContactsAdapter extends RecyclerView.Adapter<PhoneContactsAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageView contactLetter;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            contactLetter = itemView.findViewById(R.id.letter_image_view);
        }
    }

    private Context outsideContext;
    private ArrayList<Contact> mContacts;

    PhoneContactsAdapter(Context c, ArrayList<Contact> contacts) {
        this.outsideContext = c;

        ArrayList<ArrayList<HashMap<String, String>>> sections = Contact.sectionalRepresentation(contacts);
        mContacts = new ArrayList<>();

        for (ArrayList<HashMap<String, String>> section : sections) {
            mContacts.add(null);
            for (HashMap<String, String> item : section) {
                mContacts.add(new Contact(item));
            }
        }
    }

    @NonNull
    @Override
    public PhoneContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView;

        if (viewType == 0) {
            contactView = inflater.inflate(R.layout.item_phone_contact, parent, false);
        } else {
            contactView = inflater.inflate(R.layout.item_contact_section, parent, false);
        }

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PhoneContactsAdapter.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == 0) {
            final Contact contact = mContacts.get(position);
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

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((AddContactActivity) outsideContext).findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    ((AddContactActivity) outsideContext).findViewById(R.id.rvContacts).setEnabled(false);
                    ESPMobileAPI.addContact(outsideContext, contact, new ParsedRequestListener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ((AddContactActivity) outsideContext).finishWithMessage();
                        }

                        @Override
                        public void onError(ANError anError) {
                            ((AddContactActivity) outsideContext).findViewById(R.id.progressBar).setVisibility(View.GONE);
                            ((AddContactActivity) outsideContext).findViewById(R.id.rvContacts).setEnabled(true);
                            MessageBanner.show((Activity) outsideContext, R.string.error_message, R.color.espRed);
                        }
                    });
                }
            });
        } else {
            final Contact contact = mContacts.get(position + 1);
            TextView sectionTitle = viewHolder.itemView.findViewById(R.id.section_letter);
            sectionTitle.setText(String.valueOf(Character.toUpperCase(contact.getName().charAt(0))));
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mContacts.get(position) != null ? 0 : 1;
    }
}
