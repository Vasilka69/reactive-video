package ru.vasili4.reactivevideo.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vasili4.reactivevideo.data.model.FileItem
import ru.vasili4.reactivevideo.data.model.FileType
import ru.vasili4.reactivevideo.databinding.ItemFileBinding

class FileAdapter(
    private val listener: FileActionListener,
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private val items = mutableListOf<FileItem>()

    fun updateItems(newItems: List<FileItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class FileViewHolder(
        private val binding: ItemFileBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileItem) {
            binding.textFilePath.text = item.filePath
            binding.textBucket.text = item.bucket
            binding.textType.text = item.type.name

            when (item.type) {
                FileType.IMAGE, FileType.GIF -> {
                    binding.imageTypeIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                    binding.textTypeBadge.text = if (item.type == FileType.GIF) "GIF" else "IMG"
                }

                FileType.VIDEO -> {
                    binding.imageTypeIcon.setImageResource(android.R.drawable.ic_media_play)
                    binding.textTypeBadge.text = "MP4"
                }

                FileType.TEXT -> {
                    binding.imageTypeIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    binding.textTypeBadge.text = "TXT"
                }

                FileType.OTHER -> {
                    binding.imageTypeIcon.setImageResource(android.R.drawable.ic_menu_save)
                    binding.textTypeBadge.text = "FILE"
                }
            }

            binding.buttonPreview.visibility = if (item.type.isVisual) View.VISIBLE else View.GONE
            binding.buttonRecognize.visibility = if (item.type == FileType.IMAGE) View.VISIBLE else View.GONE
            binding.buttonRecognizeCached.visibility = if (item.type == FileType.IMAGE) View.VISIBLE else View.GONE
            binding.buttonText.visibility = if (item.type == FileType.TEXT) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                if (item.type.isVisual) {
                    listener.onPreview(item)
                } else if (item.type == FileType.TEXT) {
                    listener.onOpenText(item)
                }
            }
            binding.buttonPreview.setOnClickListener { listener.onPreview(item) }
            binding.buttonRecognize.setOnClickListener { listener.onRecognize(item, false) }
            binding.buttonRecognizeCached.setOnClickListener { listener.onRecognize(item, true) }
            binding.buttonText.setOnClickListener { listener.onOpenText(item) }
            binding.buttonDownload.setOnClickListener { listener.onDownload(item) }
            binding.buttonDelete.setOnClickListener { listener.onDelete(item) }
        }
    }
}

interface FileActionListener {
    fun onPreview(item: FileItem)
    fun onRecognize(item: FileItem, useCache: Boolean)
    fun onOpenText(item: FileItem)
    fun onDownload(item: FileItem)
    fun onDelete(item: FileItem)
}
