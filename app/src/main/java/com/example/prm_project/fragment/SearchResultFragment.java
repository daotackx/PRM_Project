package com.example.prm_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.BookAdapter;
import com.example.prm_project.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Arrays;

public class SearchResultFragment extends Fragment implements SearchSuggestionDialogFragment.OnKeywordSelectedListener {
    private static final String ARG_KEYWORD = "keyword";
    private String keyword;
    private RecyclerView rvResults;
    private TextView tvTitle;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText edSearchResult;

    public static SearchResultFragment newInstance(String keyword) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEYWORD, keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyword = getArguments().getString(ARG_KEYWORD);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        rvResults = view.findViewById(R.id.rvResults);
        tvTitle = view.findViewById(R.id.tvSearchTitle);
        edSearchResult = view.findViewById(R.id.edSearchResult);
        edSearchResult.setText(keyword);
        tvTitle.setText("Kết quả cho: " + keyword);
        setupRecyclerView();
        setupSearchBar();
        searchBooksByKeyword();
        return view;
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(bookList, book -> {
            BookDetailFragment fragment = BookDetailFragment.newInstance(book.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        int spacingInPixels = (int) getResources().getDisplayMetrics().density * 12;
        rvResults.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        rvResults.setAdapter(bookAdapter);
    }

    private void setupSearchBar() {
        edSearchResult.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableStart = 0;
                if (edSearchResult.getCompoundDrawables()[0] != null) {
                    drawableStart = edSearchResult.getCompoundDrawables()[0].getBounds().width();
                }
                // Nếu bấm vào icon search
                if (event.getX() <= (edSearchResult.getPaddingLeft() + drawableStart + edSearchResult.getCompoundDrawablePadding())) {
                    String newKeyword = edSearchResult.getText().toString().trim();
                    if (!newKeyword.isEmpty()) {
                        keyword = newKeyword;
                        tvTitle.setText("Kết quả cho: " + keyword);
                        searchBooksByKeyword();
                    }
                    return true;
                } else {
                    // Nếu bấm vào phần còn lại của EditText, mở dialog suggestion
                    SearchSuggestionDialogFragment dialog = SearchSuggestionDialogFragment.newInstance(
                        Arrays.asList("thiếu nhi", "Kinh dị", "Văn học", "Khoa học", "Tiểu sử", "Kỹ năng sống", "Elon Musk", "Nhà giả kim", "Hoàng tử bé"),
                        this
                    );
                    dialog.show(getParentFragmentManager(), "SearchSuggestionDialog");
                    return true;
                }
            }
            return false;
        });
    }

    private static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    private void searchBooksByKeyword() {
        db.collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    String keywordNoAccent = removeAccent(keyword.toLowerCase());
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        if ((book.getName() != null && removeAccent(book.getName().toLowerCase()).contains(keywordNoAccent)) ||
                            (book.getAuthor() != null && removeAccent(book.getAuthor().toLowerCase()).contains(keywordNoAccent)) ||
                            (book.getBookType() != null && book.getBookType().getName() != null &&
                             removeAccent(book.getBookType().getName().toLowerCase()).contains(keywordNoAccent)) ||
                            (book.getDescription() != null && removeAccent(book.getDescription().toLowerCase()).contains(keywordNoAccent))) {
                            bookList.add(book);
                        }
                    }
                    bookAdapter.notifyDataSetChanged();
                    if (bookList.isEmpty()) {
                        Toast.makeText(getContext(), "Không tìm thấy sách phù hợp", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onKeywordSelected(String newKeyword) {
        if (newKeyword != null && !newKeyword.isEmpty()) {
            keyword = newKeyword;
            edSearchResult.setText(keyword);
            tvTitle.setText("Kết quả cho: " + keyword);
            searchBooksByKeyword();
        }
    }
} 