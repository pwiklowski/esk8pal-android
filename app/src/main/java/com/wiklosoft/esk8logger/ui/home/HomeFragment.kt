package com.wiklosoft.esk8logger.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wiklosoft.esk8logger.R

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var connectionStatus: TextView
    private lateinit var voltage: TextView
    private lateinit var current: TextView
    private lateinit var usedEnergy: TextView
    private lateinit var totalEnergy: TextView

    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var speed: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        connectionStatus = root.findViewById(R.id.connection_status)
        voltage = root.findViewById(R.id.voltage)
        current = root.findViewById(R.id.current)
        usedEnergy = root.findViewById(R.id.used_energy)
        totalEnergy = root.findViewById(R.id.total_enegry)

        latitude = root.findViewById(R.id.latitude)
        longitude = root.findViewById(R.id.longitude)
        speed = root.findViewById(R.id.speed)

        homeViewModel.connectionState.observe(viewLifecycleOwner, Observer {
            connectionStatus.text = it?.name ?: ""
        })

        homeViewModel.voltage.observe(viewLifecycleOwner, Observer {
            voltage.text = it?.let {
                "%.2f V".format((it))
            } ?: ""
        })

        homeViewModel.current.observe(viewLifecycleOwner, Observer {
            current.text = it?.let {
                "%.2f A".format((it))
            } ?: ""
        })

        homeViewModel.usedEnergy.observe(viewLifecycleOwner, Observer {
            usedEnergy.text = it?.let {
                "%.2f mAh".format((it*1000))
            } ?: ""
        })

        homeViewModel.totalEnergy.observe(viewLifecycleOwner, Observer {
            totalEnergy.text = it?.let {
                "%.0f mAh".format((it*1000))
            } ?: ""
        })

        homeViewModel.latitude.observe(viewLifecycleOwner, Observer {
            latitude.text = it?.let {
                "%.6f".format((it))
            } ?: ""
        })

        homeViewModel.longitude.observe(viewLifecycleOwner, Observer {
            longitude.text = it?.let {
                "%.6f".format((it))
            } ?: ""
        })

        homeViewModel.speed.observe(viewLifecycleOwner, Observer {
            speed.text = it?.let {
                "%.2f km/h".format((it))
            } ?: ""
        })
        return root
    }
}
