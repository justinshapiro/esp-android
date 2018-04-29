package org.espmobile.esp_mobile.contacts;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.Contact;

import java.util.ArrayList;

public class AlertGroupsAdapter extends RecyclerView.Adapter<AlertGroupsAdapter.ViewHolder> implements View.OnLongClickListener {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        ImageView contactLetter;

        public ViewHolder(View itemView) {
            super(itemView);
            this.personName = itemView.findViewById(R.id.alert_group_person_name);
            this.contactLetter = itemView.findViewById(R.id.alert_group_initials_icon);
        }
    }

    private ArrayList<Contact> contacts;
    private Boolean listType;
    private IListener IListener;

    public AlertGroupsAdapter(ArrayList<Contact> contacts, IListener IListener, Boolean listType) {
        this.contacts = contacts;
        this.IListener = IListener;
        this.listType = listType;
    }

    @NonNull
    @Override
    public AlertGroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v;

        if (viewType == 0) {
            v = inflater.inflate(R.layout.item_existing_contact, parent, false);
        } else {
            v = inflater.inflate(R.layout.item_alert_group, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertGroupsAdapter.ViewHolder holder, int position) {
        holder.personName.setText(contacts.get(position).getName());
        holder.itemView.setTag(position);
        holder.itemView.setOnLongClickListener(this);
        holder.itemView.setOnDragListener(new DragListener(IListener));

        String letter = String.valueOf(contacts.get(position).getName().charAt(0));
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(3)
                .textColor(Color.BLACK)
                .bold()
                .endConfig()
                .buildRound(letter, Color.GRAY);
        holder.contactLetter.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (listType) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v.startDragAndDrop(data, shadowBuilder, v, 0);
        } else {
            v.startDrag(data, shadowBuilder, v, 0);
        }
        return true;
    }

    DragListener getDragInstance() {
        if (IListener != null) {
            return new DragListener(IListener);
        } else {
            Log.e("ListAdapter", "IListener wasn't initialized!");
            return null;
        }
    }

    public ArrayList<Contact> getDataSource() {
        return contacts;
    }

    public void updateDataSource(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }
}
