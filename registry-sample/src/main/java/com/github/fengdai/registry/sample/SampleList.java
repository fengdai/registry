package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.sample.binder.AddressBinder;
import com.github.fengdai.registry.sample.binder.CardBinder_TextOnly;
import com.github.fengdai.registry.sample.binder.HeadBinder;
import com.github.fengdai.registry.sample.holder.CardVH;

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
)
@interface SampleList {
}
