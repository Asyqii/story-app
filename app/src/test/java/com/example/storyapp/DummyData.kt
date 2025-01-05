package com.example.storyapp

import com.example.storyapp.data.response.ListStoryItem

object DummyData {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..50) {
            val story = ListStoryItem(
                i.toString(),
                //random image test
                "https://picsum.photos/seed/picsum/200/300",
                "Title $i",
                "Description $i",
                0.0,
                "id $i",
                0.0
            )
            items.add(story)
        }
        return items
    }
}