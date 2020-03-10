package ru.welokot.monopoly.ui.fragment.prepareforgame

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.dialog_main.*
import kotlinx.android.synthetic.main.prepare_to_game_fragment.*
import ru.welokot.monopoly.R
import ru.welokot.monopoly.db.Player
import ru.welokot.monopoly.ui.Router
import ru.welokot.monopoly.utils.ViewAnimation
import javax.inject.Inject
import kotlin.random.Random


class PrepareToGameFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    private lateinit var viewModel: PrepareToGameViewModel
    private lateinit var adapter: PrepareToGameAdapter

    private var rotate = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(this, providerFactory).get(PrepareToGameViewModel::class.java)

        return inflater.inflate(R.layout.prepare_to_game_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initListeners()
        initAdapters()
        initRecyclerView()
        initFab()
        initObservers()

        updateButtonState()
    }

    private fun initListeners() {
        fabAddNewPlayer.setOnLongClickListener {
            toggleFabMode(it)
            true
        }

        fabAddNewPlayer.setOnClickListener {
            if (!rotate) {
                showDialogAddNewPlayer()
            }
            else toggleFabMode(it)
        }

        fabAddOldPlayers.setOnClickListener {
            Toast.makeText(
                activity,
                "Скоро будет",
                Toast.LENGTH_SHORT
            ).show()
        }

        fabStartGame.setOnClickListener {
            if (it.isEnabled) {
                Router.showGameBoardFragment(activity!!.supportFragmentManager, viewModel.getPlayersList())
            }
        }

        back_drop.setOnClickListener {
            toggleFabMode(fabAddNewPlayer)
        }
    }

    private fun initAdapters() {
        adapter = PrepareToGameAdapter(context!!)
    }

    private fun initRecyclerView() {
        rvPlayersList.layoutManager = LinearLayoutManager(context)
        rvPlayersList.adapter = adapter

        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.deletePlayer(viewHolder.adapterPosition)
                updateButtonState()
            }

        }

        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(rvPlayersList)
    }

    private fun initObservers() {
        viewModel.playersListLiveData.observe(viewLifecycleOwner, Observer {
            adapter.updatePlayersList(it)
        })

    }

    @SuppressLint("Recycle")
    private fun showDialogAddNewPlayer() {
        val dialog = Dialog(context!!)

        dialog.apply {
            setContentView(R.layout.dialog_main)
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            window!!.attributes.windowAnimations = R.style.Animation_Design_BottomSheetDialog

            btnAction.setOnClickListener {
                if (it.isEnabled) {
                    viewModel.addPlayer(
                        Player(
                            name = tiedName.text.toString(),
                            capital = if (tiedCapital.text.isNullOrEmpty()) 0.0 else tiedCapital.text.toString().toDouble(),
                            icon = Random.nextInt(context.resources.obtainTypedArray(R.array.player_icon).length())
                        )
                    )
                    tiedName.setText("")
                }
                updateButtonState()
            }

            btnCloseDialog.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun initFab() {
        ViewAnimation.initShowOut(clAddOldPlayers)
        back_drop.visibility = View.GONE
    }

    private fun toggleFabMode(v: View) {
        rotate = ViewAnimation.rotateFab(v, !rotate)
        if (rotate) {
            ViewAnimation.showIn(clAddOldPlayers)
            back_drop.visibility = View.VISIBLE
        } else {
            ViewAnimation.showOut(clAddOldPlayers)
            back_drop.visibility = View.GONE
        }
    }

    fun updateButtonState() {
        fabStartGame.isEnabled = viewModel.getPlayersList().size >= 2
    }
}