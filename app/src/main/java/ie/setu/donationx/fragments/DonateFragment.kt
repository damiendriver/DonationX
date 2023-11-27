package ie.setu.donationx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import ie.setu.donationx.R
import ie.setu.donationx.databinding.FragmentDonateBinding
import ie.setu.donationx.main.DonationXApp
import ie.setu.donationx.models.DonationModel

/**
 * A simple [Fragment] subclass.
 * Use the [DonateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DonateFragment : Fragment() {

    lateinit var app: DonationXApp
    var totalDonated = 0
    private var _fragBinding: FragmentDonateBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationXApp
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentDonateBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.action_donate)

        fragBinding.progressBar.max = 10000
        fragBinding.amountPicker.minValue = 1
        fragBinding.amountPicker.maxValue = 1000

        fragBinding.amountPicker.setOnValueChangedListener { _, _, newVal ->
            //Display the newly selected number to paymentAmount
            fragBinding.paymentAmount.setText("$newVal")
        }
        setButtonListener(fragBinding)
        return root;
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_donate, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
        totalDonated = app.donationsStore.findAll().sumOf { it.amount }
        fragBinding.progressBar.progress = totalDonated
        fragBinding.totalSoFar.text = getString(R.string.totalSoFar,totalDonated)
    }

    fun setButtonListener(layout: FragmentDonateBinding) {
        layout.donateButton.setOnClickListener {
            val amount = if (layout.paymentAmount.text.isNotEmpty())
                layout.paymentAmount.text.toString().toInt()
            else layout.amountPicker.value
            if(totalDonated >= layout.progressBar.max)
                Toast.makeText(
                    context, "Donate Amount Exceeded!",
                    Toast.LENGTH_LONG
                ).show()
            else {
                val paymentmethod = if(layout.paymentMethod.checkedRadioButtonId == R.id.Direct) "Direct" else "Paypal"
                totalDonated += amount
                layout.totalSoFar.text = getString(R.string.totalSoFar,totalDonated)
                layout.progressBar.progress = totalDonated
                app.donationsStore.create(
                    DonationModel(
                        paymentmethod = paymentmethod,
                        amount = amount
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DonateFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}