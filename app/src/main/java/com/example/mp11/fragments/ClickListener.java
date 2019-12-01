package com.example.mp11.fragments;

import android.view.View;

//интерфейс, чтобы обрабатывать клики на словари в фрагменте со словарями
public interface ClickListener {
    void onItemClick(int position, View v);
    void onItemLongClick(int position, View v);
}