package com.grzeluu.lookupplant.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.grzeluu.lookupplant.model.Advice;
import com.grzeluu.lookupplant.model.Plant;
import com.grzeluu.lookupplant.view.adapter.AdviceAdapter;
import com.grzeluu.lookupplant.R;
import com.grzeluu.lookupplant.utils.TranslationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AdviceBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_PLANT_ID = "arg_plant_id";
    private static final String ARG_PLANT_NAME = "arg_plant_name";

    private String plantId;
    private String plantName;

    private TextView titleTv;
    private RecyclerView rvAdvices;
    private TextView emptyTv;
    private AdviceAdapter adviceAdapter;
    private List<Advice> adviceList = new ArrayList<>();

    public static AdviceBottomSheetDialogFragment newInstance(String plantId, String plantName) {
        AdviceBottomSheetDialogFragment f = new AdviceBottomSheetDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PLANT_ID, plantId);
        b.putString(ARG_PLANT_NAME, plantName);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_advices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        titleTv = view.findViewById(R.id.bs_title);
        rvAdvices = view.findViewById(R.id.rv_advices_sheet);
        emptyTv = view.findViewById(R.id.empty_advices);

        rvAdvices.setLayoutManager(new LinearLayoutManager(getContext()));
        adviceAdapter = new AdviceAdapter(getContext(), adviceList);
        rvAdvices.setAdapter(adviceAdapter);

        if (getArguments() != null) {
            plantId = getArguments().getString(ARG_PLANT_ID);
            plantName = getArguments().getString(ARG_PLANT_NAME, "");
            titleTv.setText(plantName);
        }

        loadAdvices();
    }

    private void loadAdvices() {
        if (plantId == null) {
            showEmpty();
            return;
        }

        DatabaseReference plantRef = FirebaseDatabase.getInstance()
                .getReference("Plants")
                .child(plantId);

        plantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Plant plant = snapshot.getValue(Plant.class);

                if (plant == null || plant.getAdvicesList() == null || plant.getAdvicesList().isEmpty()) {
                    showEmpty();
                    return;
                }

                adviceList.clear();
                adviceList.addAll(plant.getAdvicesList().values());

                String srcLang = (plant.getOriginalLanguage() != null && !plant.getOriginalLanguage().isEmpty())
                        ? plant.getOriginalLanguage()
                        : "en";
                String targetLang = Locale.getDefault().getLanguage();

                if (srcLang.equals(targetLang)) {
                    adviceAdapter.notifyDataSetChanged();
                    showList();
                    return;
                }

                adviceAdapter.notifyDataSetChanged();
                showList();

                translateAdvices(adviceList, srcLang, targetLang);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmpty();
            }
        });
    }

    private void translateAdvices(List<Advice> advices, String srcLang, String targetLang) {
        if (getContext() == null) return;

        AtomicInteger remaining = new AtomicInteger(advices.size() * 2); // вопрос + ответ

        for (Advice advice : advices) {
            String qText = advice.getQuestion() != null ? advice.getQuestion() : "";
            String aText = advice.getAnswer() != null ? advice.getAnswer() : "";

            TranslationHelper.translate(getContext(), srcLang, targetLang, qText,
                    new TranslationHelper.Callback() {
                        @Override
                        public void onComplete(String translated) {
                            advice.setQuestion(translated != null ? translated : qText);
                            if (remaining.decrementAndGet() == 0 && isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        adviceAdapter.notifyDataSetChanged()
                                );
                            }
                        }
                    });

            TranslationHelper.translate(getContext(), srcLang, targetLang, aText,
                    new TranslationHelper.Callback() {
                        @Override
                        public void onComplete(String translated) {
                            advice.setAnswer(translated != null ? translated : aText);
                            if (remaining.decrementAndGet() == 0 && isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        adviceAdapter.notifyDataSetChanged()
                                );
                            }
                        }
                    });
        }
    }

    private void showEmpty() {
        emptyTv.setVisibility(View.VISIBLE);
        rvAdvices.setVisibility(View.GONE);
    }

    private void showList() {
        emptyTv.setVisibility(View.GONE);
        rvAdvices.setVisibility(View.VISIBLE);
    }
}