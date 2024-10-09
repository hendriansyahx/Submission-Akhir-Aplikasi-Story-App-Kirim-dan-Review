package com.callcenter.storydicoding.view.story.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.databinding.ItemStoryBinding
import com.squareup.picasso.Picasso

class StoryAdapter(
    private val onItemClick: (String) -> Unit
) : PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Story,
                newItem: Story
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.tvName.text = story.name
            binding.tvDescription.text = truncateDescription(story.description, 100)

            binding.progressBar.visibility = View.VISIBLE

            Picasso.get()
                .load(story.photoUrl)
                .into(binding.ivStory, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        binding.progressBar.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        binding.progressBar.visibility = View.GONE
                    }
                })

            binding.root.setOnClickListener {
                onItemClick(story.id)
            }
        }

        private fun truncateDescription(description: String, maxLength: Int): String {
            return if (description.length > maxLength) {
                description.take(maxLength) + "..."
            } else {
                description
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }
}

