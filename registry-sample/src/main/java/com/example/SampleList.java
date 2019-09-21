package com.example;

import com.example.binder.AddressBinder;
import com.example.binder.CardBinder_TextOnly;
import com.example.binder.HeadBinder;
import com.example.holder.CardVH;
import com.github.fengdai.registry.Register;

@Register(
    binders = {
        AddressBinder.class,
        CardBinder_TextOnly.class,
        HeadBinder.class
    },
    binderViewHolders = {
        CardVH.class
    },
    staticContentLayouts = {
        R.layout.gap
    }
) @interface SampleList {
}
