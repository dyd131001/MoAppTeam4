package com.example.todayseat.ui.preference

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todayseat.MainActivity
import com.example.todayseat.SplashActivity
import com.example.todayseat.databinding.FragmentPreferenceBinding

class PreferenceFragment : Fragment() {

    private var _binding: FragmentPreferenceBinding? = null
    private val binding get() = _binding!!
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferenceBinding.inflate(inflater, container, false)
        val menus= mutableListOf<Menu>()
        menus.add(Menu("https://recipe1.ezmember.co.kr/cache/recipe/2022/11/20/c8e66a6cc996829f1da8c339fd1bca2b1.jpg",
            "제육볶음","고기"))
        menus.add(Menu("https://recipe1.ezmember.co.kr/cache/recipe/2017/02/21/8147779d6a47ae304957c86f1afe58321.jpg",
        "김치볶음밥","밥"))

        Log.d("TAG11","before binding adapter")

        val sql = "SELECT F_name FROM FOOD"
        val c: Cursor = SplashActivity.moappDB.rawQuery(sql,null)
        while (c.moveToNext()){
            val F_ID_pos = c.getColumnIndex("F_name")
            menus.add(Menu("https://recipe1.ezmember.co.kr/cache/recipe/2017/02/21/8147779d6a47ae304957c86f1afe58321.jpg",
                c.getString(F_ID_pos),"밥"))
        }

        val preferMenuListAdpater =PreferMenuListAdapter(menus)
        binding.preferRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        binding.preferRecyclerView.adapter=preferMenuListAdpater

        binding.preferSearchView.setOnQueryTextListener(object :android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.d("TAG11","text change!!")
                preferMenuListAdpater.filter.filter(p0)
                return true
            }

        })

        val root: View = binding.root



        return root
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //여기에 백버튼 눌렀을때 적용될것 작성
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}