package com.example.todoapp;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private boolean subtitleVisible=false;
    public static final String KEY_EXTRA_TASK_ID = "tasklistfragment.task_id";
    public static final String KEY_COUNT_VISIBLE = "subtitleVisible";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_task_list,container,false);
        recyclerView=view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }
    private void updateView(){
        TaskStorage taskStorage=TaskStorage.getInstance();
        List<Task> tasks=taskStorage.getTasks();
        if(adapter==null){
            adapter=new TaskAdapter(tasks);
            recyclerView.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_menu,menu);
        MenuItem subtitleItem=menu.findItem(R.id.show_subtitles);
        if(subtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null) subtitleVisible=savedInstanceState.getBoolean(KEY_COUNT_VISIBLE);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();
        if(itemId==R.id.new_task) {
            Task task = new Task();
            TaskStorage.getInstance().addTask(task);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(TaskListFragment.KEY_EXTRA_TASK_ID, task.getId());
            startActivity(intent);
            return true;
        }
        if(itemId==R.id.show_subtitles){
            subtitleVisible=!subtitleVisible;
            getActivity().invalidateOptionsMenu();
            updateSubtitle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void updateSubtitle(){
        TaskStorage taskStorage=TaskStorage.getInstance();
        List<Task> tasks=taskStorage.getTasks();
        int todoTaskCount=0;
        for(Task task:tasks){
            if(!task.isDone()) todoTaskCount++;
        }
        String subtitle=getString(R.string.subtitle_format,todoTaskCount);
        if(!subtitleVisible) subtitle=null;
        AppCompatActivity appCompatActivity=(AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_COUNT_VISIBLE,subtitleVisible);
    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView nameTextView,dateTextView;
        private CheckBox checkBox;
        private Task task;
        private ImageView iconImageView;
        public TaskHolder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_task,parent,false));
            itemView.setOnClickListener(this);
            nameTextView=itemView.findViewById(R.id.task_item_name);
            dateTextView=itemView.findViewById(R.id.task_item_date);
            checkBox=itemView.findViewById(R.id.task_item_checkbox);
            iconImageView=itemView.findViewById(R.id.task_item_category);
        }
        public void bind(Task task){
            this.task=task;
            nameTextView.setText(task.getName());
            dateTextView.setText(task.getDate().toString());
            checkBox.setChecked(task.isDone());
            if(task.isDone()) nameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            if(task.getCategory().equals(Category.DOM)){
                iconImageView.setImageResource(R.drawable.ic_house);
            }else{
                iconImageView.setImageResource(R.drawable.ic_studies);
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent=new Intent(getActivity(),MainActivity.class);
            intent.putExtra(KEY_EXTRA_TASK_ID,task.getId());
            startActivity(intent);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public TextView getNameTextView() {
            return nameTextView;
        }
    }
    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder>{
        private List<Task> tasks;
        public TaskAdapter(List<Task> tasks){
            this.tasks=tasks;
        }

        @NonNull
        @Override
        public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            return new TaskHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
            Task task=tasks.get(position);
            CheckBox checkBox=holder.getCheckBox();
            TextView nameTextView=holder.getNameTextView();
            checkBox.setChecked(tasks.get(position).isDone());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
            {
                tasks.get(holder.getBindingAdapterPosition()).setDone(isChecked);
                if(isChecked) nameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                else nameTextView.setPaintFlags(0);
            });
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }

}
