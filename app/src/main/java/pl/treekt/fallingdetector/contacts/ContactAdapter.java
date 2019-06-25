package pl.treekt.fallingdetector.contacts;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.treekt.fallingdetector.ContactActivity;
import pl.treekt.fallingdetector.R;
import pl.treekt.fallingdetector.data.DetectorContract;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactsAdapterViewHolder> {

    private Integer currentSelectionId;
    private Cursor mCursor;
    private Context mContext;
    private OnContactSelectionListener contactSelectionListener;

    public ContactAdapter(@NonNull Context context) {
        this.mContext = context;

        if (context instanceof OnContactSelectionListener) {
            this.contactSelectionListener = (OnContactSelectionListener) context;
        }
    }

    public Integer getCurrentSelectionId() {
        return currentSelectionId;
    }

    public void setCurrentSelectionId(Integer currentSelectionId) {
        this.currentSelectionId = currentSelectionId;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return;
        }

        if (cursor != null) {
            this.mCursor = cursor;
            this.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ContactsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.contact_list_item, parent, false);

        return new ContactsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapterViewHolder holder, int position) {
        //todo efficeciency improvement, move to ContactActivity.
        int idIndex = mCursor.getColumnIndex(DetectorContract.DetectorEntry._ID);
        int numberIndex = mCursor.getColumnIndex(DetectorContract.DetectorEntry.COLUMN_NUMBER);
        int nameIndex = mCursor.getColumnIndex(DetectorContract.DetectorEntry.COLUMN_NAME);
        int surnameIndex = mCursor.getColumnIndex(DetectorContract.DetectorEntry.COLUMN_SURNAME);
        int selectionIndex = mCursor.getColumnIndex(DetectorContract.DetectorEntry.COLUMN_SELECTED);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        int number = mCursor.getInt(numberIndex);
        int selection = mCursor.getInt(selectionIndex);
        String name = mCursor.getString(nameIndex);
        String surname = mCursor.getString(surnameIndex);

        holder.itemView.setTag(id);
        holder.contactNameTv.setText(name);
        holder.contactSurnameTv.setText(surname);
        holder.contactNumberTv.setText(String.valueOf(number));

        if (selection != ContactActivity.NOT_SELECTED_INT) {
            currentSelectionId = id;
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSelectedContact));
            holder.isSelected = true;
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDefaultBackground));
            holder.isSelected = false;
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public interface OnContactSelectionListener {
        void onSelectionChanged(ContactsAdapterViewHolder viewHolder);
    }

    public class ContactsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        @BindView(R.id.tv_contact_name)
        TextView contactNameTv;
        @BindView(R.id.tv_contact_surname)
        TextView contactSurnameTv;
        @BindView(R.id.tv_contact_number)
        TextView contactNumberTv;

        private Boolean isSelected;

        ContactsAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            isSelected = !isSelected;
            contactSelectionListener.onSelectionChanged(this);
            return true;
        }

        public int getNumber() {
            return Integer.valueOf(contactNumberTv.getText().toString());
        }

        public Boolean getSelected() {
            return isSelected;
        }

        public void setSelected(Boolean selected) {
            isSelected = selected;
        }
    }
}
