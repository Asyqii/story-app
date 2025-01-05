package com.example.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyapp.DummyData
import com.example.storyapp.MainDispatcherRule
import com.example.storyapp.getOrAwaitValue
import com.example.storyapp.data.adapter.StoryAdapter
import com.example.storyapp.data.paging.StoryPagingSource
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.ListStoryItem
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository
    private lateinit var mainViewModel: MainViewModel

    @Test
    fun `verify Get Story is Not Null and Returns Correct Data`() = runTest {
        val dummyData = DummyData.generateDummyStoryResponse()
        val pagingData: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyData)
        val liveDataStories = MutableLiveData<PagingData<ListStoryItem>>().apply {
            value = pagingData
        }
        Mockito.`when`(userRepository.getStory()).thenReturn(liveDataStories)

        mainViewModel = MainViewModel(userRepository)
        val actualStory: PagingData<ListStoryItem> = mainViewModel.getStory.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFFUTIL,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyData.size, differ.snapshot().size)
        assertEquals(dummyData[0], differ.snapshot()[0])
    }

    @Test
    fun `verify Get Story Empty Returns No Data`() = runTest {
        val emptyPagingData: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val liveDataStories = MutableLiveData<PagingData<ListStoryItem>>().apply {
            value = emptyPagingData
        }
        Mockito.`when`(userRepository.getStory()).thenReturn(liveDataStories)

        mainViewModel = MainViewModel(userRepository)
        val actualStory: PagingData<ListStoryItem> = mainViewModel.getStory.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFFUTIL,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}