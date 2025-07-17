package com.example.prm_project.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchSuggestionDialogFragment extends DialogFragment {
    public interface OnKeywordSelectedListener {
        void onKeywordSelected(String keyword);
    }

    private OnKeywordSelectedListener listener;
    private EditText edSearch;
    private RecyclerView rvSuggestions;
    private SuggestionAdapter adapter;
    private List<String> allKeywords;
    private List<String> filteredKeywords = new ArrayList<>();

    public SearchSuggestionDialogFragment() {}

    public static SearchSuggestionDialogFragment newInstance(List<String> keywords, OnKeywordSelectedListener listener) {
        SearchSuggestionDialogFragment fragment = new SearchSuggestionDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("keywords", new ArrayList<>(keywords));
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_suggestion, container, false);
        edSearch = view.findViewById(R.id.edSearchKeyword);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);

        // Bắt sự kiện click vào icon search trong EditText
        edSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableStart = 0;
                if (edSearch.getCompoundDrawables()[0] != null) {
                    drawableStart = edSearch.getCompoundDrawables()[0].getBounds().width();
                }
                if (event.getX() <= (edSearch.getPaddingLeft() + drawableStart + edSearch.getCompoundDrawablePadding())) {
                    String keyword = edSearch.getText().toString().trim();
                    if (!keyword.isEmpty() && listener != null) {
                        listener.onKeywordSelected(keyword);
                        dismiss();
                    }
                    return true;
                }
            }
            return false;
        });

        if (getArguments() != null) {
            allKeywords = getArguments().getStringArrayList("keywords");
        } else {
            allKeywords = Arrays.asList("thiếu nhi", "Kinh dị", "Văn học", "Khoa học", "Tiểu sử", "Kỹ năng sống", "Nguyễn Nhật Ánh", "Steve Jobs", "Hoàng tử bé");
        }
        filteredKeywords.clear();
        filteredKeywords.addAll(allKeywords);

        adapter = new SuggestionAdapter(filteredKeywords, keyword -> {
            if (listener != null) {
                listener.onKeywordSelected(keyword);
            }
            dismiss();
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(adapter);

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKeywords(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filterKeywords(String query) {
        filteredKeywords.clear();
        if (query.isEmpty()) {
            filteredKeywords.addAll(allKeywords);
        } else {
            for (String keyword : allKeywords) {
                if (keyword.toLowerCase().contains(query.toLowerCase())) {
                    filteredKeywords.add(keyword);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {
        private final List<String> keywords;
        private final OnKeywordClickListener listener;
        interface OnKeywordClickListener { void onKeywordClick(String keyword); }
        SuggestionAdapter(List<String> keywords, OnKeywordClickListener listener) {
            this.keywords = keywords;
            this.listener = listener;
        }
        @NonNull
        @Override
        public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_suggestion, parent, false);
            return new SuggestionViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
            String keyword = keywords.get(position);
            holder.tvKeyword.setText(keyword);
            holder.itemView.setOnClickListener(v -> listener.onKeywordClick(keyword));
        }
        @Override
        public int getItemCount() { return keywords.size(); }
        static class SuggestionViewHolder extends RecyclerView.ViewHolder {
            TextView tvKeyword;
            SuggestionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvKeyword = itemView.findViewById(R.id.tvKeyword);
            }
        }
    }
} 