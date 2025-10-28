package com.grzeluu.lookupplant.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grzeluu.lookupplant.base.BaseFragment;
import com.grzeluu.lookupplant.core.discover.DiscoverContract;
import com.grzeluu.lookupplant.core.discover.DiscoverPresenter;
import com.grzeluu.lookupplant.databinding.FragmentDiscoverBinding;
import com.grzeluu.lookupplant.model.Plant;
import com.grzeluu.lookupplant.view.adapter.DiscoverAdapter;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends BaseFragment implements DiscoverContract.View {

    private FragmentDiscoverBinding binding;
    private DiscoverPresenter presenter;
    private RecyclerView.Adapter discoverAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentDiscoverBinding.inflate(getLayoutInflater());
        presenter = new com.grzeluu.lookupplant.core.discover.DiscoverPresenter(this);

        init();

        return binding.getRoot();
    }

    private void init() {
        initAdapter();
        initViews();
        presenter.getAllPlants();
    }

    private void initViews() {

        binding.toolbar.etSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.getMatchingPlants(textView.getText().toString());
                return true;
            }
            return false;
        });

        binding.toolbar.btSearch.setOnClickListener(v ->
                presenter.getMatchingPlants(
                        binding.toolbar.etSearch.getText().toString())
        );
    }

    private void initAdapter() {
        RecyclerView.LayoutManager plantsLayoutManager = new LinearLayoutManager(getContext());
        binding.rvPlants.setLayoutManager(plantsLayoutManager);

        discoverAdapter = new DiscoverAdapter(getContext(), new ArrayList<Plant>());
        binding.rvPlants.setAdapter(discoverAdapter);
    }

    @Override
    public void setDiscoverPlantList(List<Plant> plantList) {
        if (plantList != null) {
            discoverAdapter = new DiscoverAdapter(getContext(), plantList);
            binding.rvPlants.setAdapter(discoverAdapter);
            discoverAdapter.notifyDataSetChanged();
        }
    }
}