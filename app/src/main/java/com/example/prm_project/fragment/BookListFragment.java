package com.example.prm_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.BookAdapter;
import com.example.prm_project.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String categoryId;
    private String categoryName;
    private RecyclerView rvBooks;
    private TextView tvTitle;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private FirebaseFirestore db;
    private ImageView btnBack;

    public static BookListFragment newInstance(String categoryId, String categoryName) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        rvBooks = view.findViewById(R.id.rvBooks);
        tvTitle = view.findViewById(R.id.tvCategoryTitle);
        btnBack = view.findViewById(R.id.btnBack);
        tvTitle.setText("Thể loại: " + categoryName);
        setupRecyclerView();
        setupClickListeners();
        loadBooksByCategory();
        return view;
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(bookList, book -> {
            // Xử lý khi click vào sách (có thể mở BookDetailFragment)
            BookDetailFragment fragment = BookDetailFragment.newInstance(book.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        int spacingInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        rvBooks.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        rvBooks.setAdapter(bookAdapter);
    }

    private void loadBooksByCategory() {
        db.collection("books")
                .whereEqualTo("bookType.id", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        bookList.add(book);
                    }
                    bookAdapter.notifyDataSetChanged();
                    if (bookList.isEmpty()) {
                        Toast.makeText(getContext(), "Không có sách thuộc thể loại này", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
} 