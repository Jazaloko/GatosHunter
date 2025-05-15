package com.example.gatoshunter.clases

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AvatarAdapter(private val context: Context, private val avatarIds: IntArray) : BaseAdapter() {

    override fun getCount(): Int = avatarIds.size

    override fun getItem(position: Int): Any = avatarIds[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView = convertView as? ImageView ?: ImageView(context)

        val size = (context.resources.displayMetrics.density * 72).toInt() // 72dp
        imageView.layoutParams = ViewGroup.LayoutParams(size, size)
        imageView.setPadding(16, 16, 16, 16)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(avatarIds[position])

        return imageView
    }
}
