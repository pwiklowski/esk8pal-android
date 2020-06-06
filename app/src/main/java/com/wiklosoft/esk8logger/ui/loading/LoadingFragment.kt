package com.wiklosoft.esk8logger.ui.loading

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wiklosoft.esk8logger.ConnectionState
import com.wiklosoft.esk8logger.MainActivity
import com.wiklosoft.esk8logger.R
import kotlinx.android.synthetic.main.loading_fragment.view.*


class LoadingFragment : Fragment() {

    companion object {
        fun newInstance() = LoadingFragment()
    }

    private lateinit var viewModel: LoadingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.loading_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(LoadingViewModel::class.java)
        (root.loading_animation.background as AnimationDrawable).start()
        return root
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectionState.observe(viewLifecycleOwner, Observer {
            Log.d("LoadingFragment", "connection status ${it.toString()}")
            if (it == ConnectionState.INITIALIZED) {
                openMainActivity()
            }
        })
    }

    fun openMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}
