package com.android.tfg.view.more;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.tfg.R;
import com.android.tfg.adapter.MoreAdapter;
import com.android.tfg.adapter.MorePagerAdapter;
import com.android.tfg.databinding.ActivityMoreBinding;
import com.android.tfg.model.MessageModel;
import com.android.tfg.viewmodel.MoreViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Objects;

public class MoreActivity extends AppCompatActivity {

    private long since=0, until=0;
    private String device;
    private MoreViewModel moreViewModel;
    private ActivityMoreBinding binding;
    private MoreAdapter moreAdapter;
    private SELECTION CHART_SELECTED;
    private enum SELECTION{
            T,
            H,
            P,
            U
    }

    private void configViewModel(){
        /**************
         * MODEL VIEW *
         **************/
        moreViewModel = new ViewModelProvider(this).get(getString(R.string.moreViewModel), MoreViewModel.class);
        moreViewModel.registerMessagesFromDevice(device); // primera llamada para todos los mensajes
        // añadir datos al recyclerview
        final Observer<LinkedList<MessageModel>> obs = messageModels -> {
            if(messageModels.isEmpty()){
                if(moreAdapter!=null){moreAdapter.clear();}
                // hide progress
                showProgress(false);
                return; }
            configRecyclerView(messageModels);

            // hide progress
            showProgress(false);
        };
        moreViewModel.getMessages().observe(this, obs); // mensajes
    }

    // Necesario para actualizar la vista conforme a los datos de la BBDD
    private void configRecyclerView(LinkedList<MessageModel> messages){
        if(moreAdapter!=null){
            moreAdapter.updateItems(moreViewModel.getMessages().getValue());
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        moreAdapter = new MoreAdapter(messages, moreViewModel);
        binding.moreRecyclerView.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(binding.moreRecyclerView.getContext(), layoutManager.getOrientation());
        divider.getDrawable().setTint(Color.WHITE);
        divider.getDrawable().setTintMode(PorterDuff.Mode.OVERLAY);
        binding.moreRecyclerView.addItemDecoration(divider);
        binding.moreRecyclerView.setAdapter(moreAdapter);
        binding.moreRecyclerView.setLayoutManager(layoutManager);
    }

    public void configView(){
        // Device
        device=(String) Objects.requireNonNull(getIntent().getExtras()).get("device");
        setTitle(device);

        /*********
         * PAGER *
         *********/
        binding.morePager.setOffscreenPageLimit(4);
        binding.morePager.setAdapter(new MorePagerAdapter(getSupportFragmentManager(),getApplicationContext(), device));

        /*****************
         * TOGGLE BUTTON *
         *****************/
        if(CHART_SELECTED==null){CHART_SELECTED=SELECTION.T;}
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(!isChecked){return;}
            switch(checkedId){
                case R.id.toggleTemp:   binding.morePager.setCurrentItem(0);
                                        CHART_SELECTED=SELECTION.T;
                                        break;
                case R.id.toggleHum:    binding.morePager.setCurrentItem(1);
                                        CHART_SELECTED=SELECTION.H;
                                        break;
                case R.id.togglePres:   binding.morePager.setCurrentItem(2);
                                        CHART_SELECTED=SELECTION.P;
                                        break;
                case R.id.toggleUV:     binding.morePager.setCurrentItem(3);
                                        CHART_SELECTED=SELECTION.U;
                                        break;
            }
        });

        /**************************
         * FLOATING ACTION BUTTON *
         **************************/
        binding.fbDate.setOnClickListener(v -> {
            // Current time
            long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();

            // Builder
            MaterialDatePicker.Builder<Pair<Long,Long>> builder = MaterialDatePicker.Builder.dateRangePicker();

            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(R.attr.materialCalendarTheme, value,true);
            builder.setTheme(value.resourceId);
            builder.setTitleText(R.string.titleDateRange);

            // Restricciones fechas
            CalendarConstraints.Builder calendarConstraintsBuilder = new CalendarConstraints.Builder();
            calendarConstraintsBuilder.setEnd(currentTimeInMillis);
            CalendarConstraints calendarConstraints = calendarConstraintsBuilder.build();
            builder.setCalendarConstraints(calendarConstraints);

            // Establecer seleccion
            builder.setSelection(new Pair<Long,Long>(moreViewModel.getSince(), moreViewModel.getUntil()));

            // Picker
            MaterialDatePicker<Pair<Long,Long>> picker = builder.build();
            picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                @Override
                public void onPositiveButtonClick(Pair<Long, Long> selection) {
                    if(selection.first>currentTimeInMillis || selection.second>currentTimeInMillis){
                        new MaterialAlertDialogBuilder(MoreActivity.this).setTitle("ERROR").setMessage("Can't select this range !").setNeutralButton("OK", null).show();
                        return;
                    }
                    moreViewModel.setSince(selection.first);
                    moreViewModel.setUntil(selection.second);
                    moreViewModel.registerMessagesFromDevice(device);
                }
            });
            picker.show(getSupportFragmentManager(), picker.toString());
        });

    }

    public void configToolbar(){
        /***********
         * TOOLBAR *
         ***********/
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configToolbar();
        binding = ActivityMoreBinding.inflate(getLayoutInflater());

        // show progress
        showProgress(true);

        setContentView(binding.getRoot());
        configView();
        configViewModel();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Cuando se restaura el estado de la actividad restablecemos el estado de los toggle
        String selected = savedInstanceState.getString("chart_selected");
        if(selected==null){return;}
        CHART_SELECTED = SELECTION.valueOf(selected);
        switch (CHART_SELECTED){
            case T: binding.toggleTemp.setChecked(true);
                CHART_SELECTED=SELECTION.T;
                break;
            case H: binding.toggleHum.setChecked(true);
                CHART_SELECTED=SELECTION.H;
                break;
            case P: binding.togglePres.setChecked(true);
                CHART_SELECTED=SELECTION.P;
                break;
            case U: binding.toggleUV.setChecked(true);
                CHART_SELECTED=SELECTION.U;
                break;
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Cuando se guarda el estado de la actividad almacenamos el estado de los toggle
        outState.putString("chart_selected", CHART_SELECTED.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        moreViewModel.unregisterMessagesFromDevice(device); // Eliminar listener de mensajes asociado al dispositivo
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflar boton favoritos
        getMenuInflater().inflate(R.menu.more_favorites_menu, menu);

        // cargar preferencias favoritos
        if(moreViewModel.isFavorite(device)){ // Añadido a favoritos
            menu.findItem(R.id.favoriteEvent).setIcon(R.drawable.ic_favorite_checked_24dp);
            menu.findItem(R.id.favoriteEvent).setChecked(true);

        }else{ // eliminado de favoritos
            menu.findItem(R.id.favoriteEvent).setIcon(R.drawable.ic_favorite_24dp);
            menu.findItem(R.id.favoriteEvent).setChecked(false);
        }

        // capturar el evento fav
        menu.findItem(R.id.favoriteEvent).setOnMenuItemClickListener(item -> {
            if(!item.isChecked()){ // Cambiar a añadido a favoritos
                addFav(item);
            }else{ // Cambiar a eliminado de favoritos
                removeFav(item);
            }
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void addFav(MenuItem item){
        // configurar icono
        item.setIcon(R.drawable.ic_favorite_checked_24dp);
        item.setChecked(true);

        // añadir a favoritos
        moreViewModel.add2Favorites(device);
        Toast.makeText(getApplicationContext(), getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show();
    }

    private void removeFav(MenuItem item){
        // configurar icono
        item.setIcon(R.drawable.ic_favorite_24dp);
        item.setChecked(false);

        // eliminar de favoritos
        moreViewModel.removeFromFavorites(device);
        Toast.makeText(getApplicationContext(), getString(R.string.remove_from_favorites), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(moreAdapter!=null){moreAdapter.clear();}
        finish();
        return true;
    }

    public void showProgress(boolean show){
        if(show){
            binding.loadStub.inflate();
            binding.charts.setVisibility(View.INVISIBLE);
        }else{
            binding.loadStub.setVisibility(View.GONE);
            binding.charts.setVisibility(View.VISIBLE);
        }
    }

}
