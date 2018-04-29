package org.espmobile.esp_mobile.contacts;

import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.View;

import java.util.ArrayList;

import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.Contact;

public class DragListener implements View.OnDragListener {

    private boolean isDropped = false;
    private IListener IListener;

    DragListener(IListener IListener) {
        this.IListener = IListener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                isDropped = true;
                int positionTarget = -1;

                View viewSource = (View) event.getLocalState();
                int viewId = v.getId();
                final int tvEmptyListTop = R.id.non_alert_groups_prompt;
                final int tvEmptyListBottom = R.id.current_alert_group_prompt;
                final int rvTop = R.id.rvExistingContacts;
                final int rvBottom = R.id.rvAlertGroup;

                switch (viewId) {
                    case tvEmptyListTop:
                    case tvEmptyListBottom:
                    case rvTop:
                    case rvBottom:

                        RecyclerView target;
                        switch (viewId) {
                            case tvEmptyListTop:
                            case rvTop:
                                target = v.getRootView().findViewById(rvTop);
                                break;
                            case tvEmptyListBottom:
                            case rvBottom:
                                target = v.getRootView().findViewById(rvBottom);
                                break;
                            default:
                                target = (RecyclerView) v.getParent();
                                positionTarget = (int) v.getTag();
                        }

                        if (viewSource != null) {
                            RecyclerView source = (RecyclerView) viewSource.getParent();

                            AlertGroupsAdapter adapterSource = (AlertGroupsAdapter) source.getAdapter();
                            int positionSource = (int) viewSource.getTag();
                            int sourceId = source.getId();

                            Contact contact = adapterSource.getDataSource().get(positionSource);
                            ArrayList<Contact> listSource = adapterSource.getDataSource();

                            listSource.remove(positionSource);
                            adapterSource.updateDataSource(listSource);
                            adapterSource.notifyDataSetChanged();

                            AlertGroupsAdapter adapterTarget = (AlertGroupsAdapter) target.getAdapter();
                            ArrayList<Contact> customListTarget = adapterTarget.getDataSource();
                            if (positionTarget >= 0) {
                                customListTarget.add(positionTarget, contact);
                            } else {
                                customListTarget.add(contact);
                            }
                            adapterTarget.updateDataSource(customListTarget);
                            adapterTarget.notifyDataSetChanged();

                            if (sourceId == rvBottom && adapterSource.getItemCount() < 1) {
                                IListener.setEmptyListBottom(true);
                            }
                            if (viewId == tvEmptyListBottom) {
                                IListener.setEmptyListBottom(false);
                            }
                            if (sourceId == rvTop && adapterSource.getItemCount() < 1) {
                                IListener.setEmptyListTop(true);
                            }
                            if (viewId == tvEmptyListTop) {
                                IListener.setEmptyListTop(false);
                            }
                        }
                        break;
                }
                break;
        }

        if (!isDropped && event.getLocalState() != null) {
            ((View) event.getLocalState()).setVisibility(View.VISIBLE);
        }
        return true;
    }
}