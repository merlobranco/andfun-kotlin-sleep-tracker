/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    private lateinit var viewModel: SleepTrackerViewModel
    private lateinit var viewModelFactory: SleepTrackerViewModelFactory

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        // Getting a reference of the application
        val application = requireNotNull(this.activity).application

        // Getting a reference to the DAO of the database
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // Creating an instance of the ViewModelFactory
        viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        // Getting a reference to the SleepTrackerViewModel
        viewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        binding.sleepTrackerViewModel = viewModel
        binding.lifecycleOwner = this

        // Telling to the recycle view to use the provided adapter to display items on the screen
        val adapter = SleepNightAdapter(SleepNightListener { nightId ->
            Toast.makeText(context, "${nightId}", Toast.LENGTH_LONG).show()
        })
        binding.sleepList.adapter = adapter

        // Adding GridLayout.
        // Telling to the recycle view to use the provided layout manager to display items on the screen
        val manager = GridLayoutManager(activity, 3)
        binding.sleepList.layoutManager = manager

        // By using the viewLifecycleOwner we are making sure this observer only around
        // while the recycle view is still on screen
        viewModel.nights.observe(viewLifecycleOwner, Observer {
            nights ->
            nights?.let {
                adapter.submitList(nights)
            }
        })

        // Observing the NavigateToSleepQuality event
        viewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer {
            night ->
            // Equivalent to if(night != null) {...} for mutable properties
            night?.let {
                val action = SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                findNavController().navigate(action)
                viewModel.doneNavigating()
            }
        })

        viewModel.showSnackBarEvent.observe(this, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                viewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }
}
