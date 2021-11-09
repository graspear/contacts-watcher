package com.example.sqcontacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>{

    List<ContactModel> contactModels;
    Context context;
    public ContactsAdapter(Context context, List<ContactModel> contactModels) {
        this.context=context;
        this.contactModels=contactModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(contactModels.get(position).getName());
        holder.number.setText(contactModels.get(position).getNumber());
    }

    @Override
    public int getItemCount() {
        return contactModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,number;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.names);
            number=itemView.findViewById(R.id.number);
        }
    }
}