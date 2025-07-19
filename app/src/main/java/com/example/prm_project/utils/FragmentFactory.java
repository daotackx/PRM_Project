package com.example.prm_project.utils;

import androidx.fragment.app.Fragment;

import com.example.prm_project.fragment.AccountFragment;
import com.example.prm_project.fragment.CartFragment;
import com.example.prm_project.fragment.HomeFragment;
import com.example.prm_project.fragment.LoginFragment;
import com.example.prm_project.fragment.MapFragment;
import com.example.prm_project.fragment.NotificationFragment;

public class FragmentFactory {

    public enum FragmentType {
        HOME, CART, ACCOUNT, LOGIN, MAP, NOTIFICATION
    }

    public static Fragment createFragment(FragmentType type) {
        switch (type) {
            case HOME:
                return new HomeFragment();
            case CART:
                return new CartFragment();
            case ACCOUNT:
                return new AccountFragment();
            case LOGIN:
                return new LoginFragment();
            case MAP:
                return new MapFragment();
            case NOTIFICATION:
                return new NotificationFragment();
            default:
                throw new IllegalArgumentException("Unknown fragment type: " + type);
        }
    }
}