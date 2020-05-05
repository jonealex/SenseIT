package com.android.tfg.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tfg.R;
import com.android.tfg.databinding.ItemSearchBinding;
import com.android.tfg.model.DeviceModel;
import com.android.tfg.view.More.MoreActivity;
import com.android.tfg.viewmodel.MainViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchBinding binding;
    private MainViewModel mainViewModel;

    public SearchViewHolder(@NonNull ItemSearchBinding itemSearchBinding, @NonNull MainViewModel mainViewModel){
        super(itemSearchBinding.getRoot());
        this.binding=itemSearchBinding;
        this.mainViewModel=mainViewModel;
    }

    public void bind(DeviceModel device){
        /***************
         * DEVICE INFO *
         ***************/
        binding.itemTitle.setText(device.getId());
        binding.itemLocation.setText(device.getName());
        binding.itemTemp.setText(String.valueOf(mainViewModel.convertTemp(device.getLastMessage().getTemp())));
        binding.itemHum.setText(String.valueOf(device.getLastMessage().getHum()));
        binding.itemPres.setText(String.valueOf(mainViewModel.convertPres(device.getLastMessage().getPres())));
        binding.itemUv.setText(String.valueOf(mainViewModel.convertUv(device.getLastMessage().getUv())));
        Date lastUpdated = new Date(device.getLastMessage().getDate().getSeconds()*1000L);
        SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        binding.itemLastUpdate.setText(mFormat.format(lastUpdated));

        /**************
         * DEVICE MAP *
         **************/
        binding.itemMap.onCreate(null);
        binding.itemMap.onResume();
        binding.itemMap.onLowMemory();
        binding.itemMap.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(device.getSite());
            googleMap.addMarker(markerOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(device.getSite()));
            //googleMap.setMinZoomPreference(15);
        });

        /**************
         * DEVICE FAV *
         **************/
        if(mainViewModel.isFavorite(device.getId())){
            binding.itemFav.setColorFilter(Color.RED);
        }else{
            binding.itemFav.setColorFilter(Color.LTGRAY);
        }
        binding.itemFav.setOnClickListener(v -> {
            if(mainViewModel.isFavorite(device.getId())){
                mainViewModel.removeFromFavorites(device.getId());
                binding.itemFav.setColorFilter(Color.LTGRAY);
                Toast.makeText(binding.getRoot().getContext(), binding.getRoot().getContext().getString(R.string.remove_from_favorites), Toast.LENGTH_SHORT).show();
            }else{
                mainViewModel.add2Favorites(device.getId());
                binding.itemFav.setColorFilter(Color.RED);
                Toast.makeText(binding.getRoot().getContext(), binding.getRoot().getContext().getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show();
            }
        });

        /**********************
         * CARD VIEW ON CLICK *
         **********************/
        binding.cardViewSearch.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), MoreActivity.class);
            i.putExtra("device", device.getId());
            v.getContext().startActivity(i);
        });
    }

}
