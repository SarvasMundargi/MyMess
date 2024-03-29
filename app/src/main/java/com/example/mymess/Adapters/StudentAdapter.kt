package com.example.mymess.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mymess.AttendanceActivity
import com.example.mymess.MainActivity
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.StudentBalanceActivity
import com.example.mymess.databinding.ListItemBinding
import com.google.android.play.integrity.internal.i

class StudentAdapter(private val items: MutableList<StudentItemModel>,
                     private val fromBalanceActivity: Boolean): RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {
    private var filteredList = ArrayList<StudentItemModel>()

    init {
        filteredList.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return StudentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val stuitem = filteredList[position]
        holder.bind(stuitem)
    }

    inner class StudentViewHolder(private val binding: ListItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(stuitem: StudentItemModel) {
            binding.profileName.text = stuitem.name
            Glide.with(binding.profilePic.context)
                .load(stuitem.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profilePic)

            val userid=stuitem.userid
            binding.root.setOnClickListener(){
                val context = binding.root.context
                val intent = if (fromBalanceActivity) {
                    Intent(context, StudentBalanceActivity::class.java)
                } else {
                    Intent(context, AttendanceActivity::class.java)
                }
                intent.putExtra("userid",userid)
                context.startActivity(intent)
            }
        }
    }

    fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(items)
        } else {
            val filterPattern = query.toLowerCase().trim()
            items.forEach { student ->
                if (student.name.toLowerCase().contains(filterPattern)) {
                    filteredList.add(student)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun update(balancelist: List<StudentItemModel>){
        items.clear()
        items.addAll(balancelist)

        notifyDataSetChanged()
    }
}