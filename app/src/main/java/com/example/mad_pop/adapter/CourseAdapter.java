package com.example.mad_pop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<Course> items = new ArrayList<>();
    private final OnCourseClickListener listener;

    public CourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Course> courses) {
        items.clear();
        items.addAll(courses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = items.get(position);
        holder.title.setText(course.getTitle());
        holder.category.setText(course.getCategory());
        holder.description.setText(course.getDescription());
        holder.mentor.setText(holder.itemView.getContext().getString(R.string.mentor_label, course.getMentorName()));
        holder.dates.setText(holder.itemView.getContext().getString(R.string.duration_label, course.getStartDate(), course.getEndDate()));
        holder.price.setText(holder.itemView.getContext().getString(R.string.course_price_value, course.getPrice()));
        holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView category;
        final TextView description;
        final TextView mentor;
        final TextView dates;
        final TextView price;

        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvCourseTitle);
            category = itemView.findViewById(R.id.tvCourseCategory);
            description = itemView.findViewById(R.id.tvCourseDescription);
            mentor = itemView.findViewById(R.id.tvMentorName);
            dates = itemView.findViewById(R.id.tvCourseDates);
            price = itemView.findViewById(R.id.tvCoursePrice);
        }
    }
}

