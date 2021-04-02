package io.getstream.chat.android.offline.querychannels

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenQuery {

    @Test
    fun `Should request query channels spec in DB`() = runBlockingTest {
        val repositories = mock<RepositoryFacade>()
        val sut = Fixture()
            .givenRepoFacade(repositories)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(repositories).selectById(any())
    }

    @Test
    fun `Given DB with query channels Should invoke selectAndEnrichChannels in ChatDomain`() = runBlockingTest {
        val chatDomainImpl = mock<ChatDomainImpl>()
        val sut = Fixture()
            .givenChatDomain(chatDomainImpl)
            .givenFailedNetworkRequest()
            .givenQueryChannelsSpec(
                QueryChannelsSpec(
                    Filters.neutral(),
                    QuerySort.Companion.desc(Channel::lastMessageAt),
                    cids = listOf("cid1", "cid2")
                )
            )
            .get()

        sut.query()

        verify(chatDomainImpl).selectAndEnrichChannels(eq(listOf("cid1", "cid2")), any())
    }

    @Test
    fun `Should request channels to chat client`() = runBlockingTest {
        val chatClient = mock<ChatClient>()
        val sut = Fixture()
            .givenChatClient(chatClient)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(chatClient).queryChannels(any())
    }

    @Test
    fun `Given channels in DB and failed network request Should return channels from DB`() = runBlockingTest {
        val dbChannels = listOf(randomChannel(cid = "cid1"), randomChannel(cid = "cid2"))
        val sut = Fixture()
            .givenFailedNetworkRequest()
            .givenQueryChannelsSpec(
                QueryChannelsSpec(
                    Filters.neutral(),
                    QuerySort.Companion.desc(Channel::lastMessageAt), cids = listOf("cid1", "cid2")
                )
            )
            .givenDBChannels(dbChannels)
            .get()

        val result = sut.query()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isEqualTo(dbChannels)
    }

    private class Fixture {
        private var chatClient: ChatClient = mock()
        private var repositories: RepositoryFacade = mock()
        private val scope: TestCoroutineScope = TestCoroutineScope()
        private var chatDomainImpl: ChatDomainImpl = mock()

        fun givenRepoFacade(repositoryFacade: RepositoryFacade) = apply {
            repositories = repositoryFacade
        }

        fun givenChatDomain(chatDomainImpl: ChatDomainImpl) = apply {
            this.chatDomainImpl = chatDomainImpl
        }

        fun givenChatClient(chatClient: ChatClient) = apply {
            this.chatClient = chatClient
        }

        fun givenFailedNetworkRequest() = apply {
            whenever(chatClient.queryChannels(any())) doReturn TestCall(Result(mock()))
        }

        suspend fun givenQueryChannelsSpec(queryChannelsSpec: QueryChannelsSpec) = apply {
            whenever(repositories.selectById(any())) doReturn QueryChannelsSpec(
                Filters.neutral(),
                QuerySort.Companion.desc(Channel::lastMessageAt),
                cids = listOf("cid1", "cid2")
            )
            whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn emptyList()
        }

        suspend fun givenDBChannels(dbChannels: List<Channel>) = apply {
            whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocationOnMock ->
                val cid = invocationOnMock.arguments[0] as String
                mock<ChannelControllerImpl> {
                    on { toChannel() } doReturn dbChannels.first { it.cid == cid }
                }
            }
            whenever(chatDomainImpl.channel(any<Channel>())) doAnswer { invocationOnMock ->
                val channel = invocationOnMock.arguments[0] as Channel
                mock<ChannelControllerImpl> {
                    on { toChannel() } doReturn channel
                }
            }
            whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn dbChannels
        }

        fun get(): QueryChannelsController {
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repositories

            return QueryChannelsController(Filters.neutral(), mock(), chatClient, chatDomainImpl)
        }
    }
}
