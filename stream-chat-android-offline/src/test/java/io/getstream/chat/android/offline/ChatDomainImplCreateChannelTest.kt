package io.getstream.chat.android.offline

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.NoRetryPolicy
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Disabled("These tests are testing CreateChannelService! Should be migrated after removing createChannel extension function")
internal class ChatDomainImplCreateChannelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val channelId = "ChannelId"
    private val channelType = "ChannelType"
    private val channelCid = "$channelType:$channelId"
    private val channelMock: Channel = mock {
        on(it.cid) doReturn channelCid
        on(it.id) doReturn channelId
        on(it.type) doReturn channelType
    }
    private val channelMembers = listOf(randomMember(), randomMember())
    private val channelExtraData = mutableMapOf<String, Any>("extraData" to true)
    private val globalMutableState = GlobalMutableState.create()

    // @Test
    // fun `given offline chat domain when creating channel should mark it with sync needed and store in database`(): Unit =
    //     runBlocking {
    //         val currentUser = randomUser()
    //         val repositoryFacade: RepositoryFacade = mockRepositoryFacade()
    //         val channel = Channel(
    //             cid = channelCid,
    //             id = channelId,
    //             type = channelType,
    //             members = channelMembers,
    //             extraData = channelExtraData,
    //         )
    //         val sut = Fixture()
    //             .givenUser(currentUser)
    //             .givenRepositoryFacade(repositoryFacade)
    //             .get()
    //
    //         globalMutableState._connectionState.value = ConnectionState.OFFLINE
    //         val result = sut.createChannel(channel).execute()
    //
    //         argumentCaptor<Channel>().apply {
    //             verify(repositoryFacade).insertChannel(capture())
    //             with(firstValue) {
    //                 syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
    //                 cid `should be equal to` channelCid
    //                 id `should be equal to` channelId
    //                 type `should be equal to` channelType
    //                 members `should be equal to` members
    //                 extraData `should be equal to` extraData
    //             }
    //         }
    //         with(result) {
    //             isSuccess `should be equal to` true
    //             with(data()) {
    //                 syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
    //                 createdBy `should be equal to` currentUser
    //                 cid `should be equal to` channelCid
    //                 members `should be equal to` channelMembers
    //                 extraData `should be equal to` channelExtraData
    //             }
    //         }
    //     }
    //
    // @Test
    // fun `given online chat domain when creating channel should store it in database `(): Unit =
    //     runBlocking {
    //         val currentUser = randomUser()
    //         val repositoryFacade: RepositoryFacade = mockRepositoryFacade()
    //         val channel = Channel(
    //             cid = channelCid,
    //             id = channelId,
    //             type = channelType,
    //             members = channelMembers,
    //             extraData = channelExtraData,
    //         )
    //         val sut = Fixture()
    //             .givenUser(currentUser)
    //             .givenRepositoryFacade(repositoryFacade)
    //             .givenChatClientResult(Result(channel))
    //             .get()
    //
    //         globalMutableState._connectionState.value = ConnectionState.CONNECTED
    //         val result = sut.createChannel(channel).execute()
    //
    //         argumentCaptor<Channel>().apply {
    //             verify(repositoryFacade, times(2)).insertChannel(capture())
    //             with(firstValue) {
    //                 cid `should be equal to` channelCid
    //                 id `should be equal to` channelId
    //                 type `should be equal to` channelType
    //                 members `should be equal to` members
    //                 extraData `should be equal to` extraData
    //             }
    //         }
    //         with(result) {
    //             isSuccess `should be equal to` true
    //             with(data()) {
    //                 syncStatus `should be equal to` SyncStatus.COMPLETED
    //                 createdBy `should be equal to` currentUser
    //                 cid `should be equal to` channelCid
    //                 members `should be equal to` channelMembers
    //                 extraData `should be equal to` channelExtraData
    //             }
    //         }
    //     }
    //
    // @Test
    // fun `given online chat domain when creating channel is completed should mark it with proper sync states`(): Unit =
    //     runBlocking {
    //         val repositoryFacade: RepositoryFacade = mockRepositoryFacade()
    //
    //         val sut = Fixture()
    //             .givenRepositoryFacade(repositoryFacade)
    //             .givenChatClientResult(Result(channelMock))
    //             .get()
    //
    //         globalMutableState._connectionState.value = ConnectionState.CONNECTED
    //         val result = sut.createChannel(channelMock).execute()
    //
    //         result.isSuccess `should be equal to` true
    //         inOrder(channelMock) {
    //             verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
    //             verify(channelMock).syncStatus = SyncStatus.COMPLETED
    //         }
    //         argumentCaptor<Channel>().apply {
    //             verify(repositoryFacade, times(2)).insertChannel(capture())
    //             with(allValues[1]) {
    //                 syncStatus = SyncStatus.COMPLETED
    //             }
    //         }
    //     }
    //
    // @Test
    // fun `given online chat domain when creating channel failed should mark it with proper sync states`(): Unit =
    //     runBlocking {
    //         val repositoryFacade: RepositoryFacade = mockRepositoryFacade()
    //
    //         val sut = Fixture()
    //             .givenRepositoryFacade(repositoryFacade)
    //             .givenChatClientResult(Result(ChatError()))
    //             .get()
    //
    //         globalMutableState._connectionState.value = ConnectionState.CONNECTED
    //         val result = sut.createChannel(channelMock).execute()
    //
    //         result.isSuccess `should be equal to` false
    //         inOrder(channelMock) {
    //             verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
    //             verify(channelMock).syncStatus = SyncStatus.SYNC_NEEDED
    //         }
    //         argumentCaptor<Channel>().apply {
    //             verify(repositoryFacade, times(2)).insertChannel(capture())
    //             with(allValues[1]) {
    //                 syncStatus = SyncStatus.SYNC_NEEDED
    //             }
    //         }
    //     }
    //
    // @Test
    // fun `given online chat domain when creating channel failed permanently should mark it with proper sync states`(): Unit =
    //     runBlocking {
    //         val repositoryFacade: RepositoryFacade = mockRepositoryFacade()
    //
    //         val sut = Fixture()
    //             .givenRepositoryFacade(repositoryFacade)
    //             .givenChatClientResult(Result(ChatNetworkError.create(code = ChatErrorCode.NETWORK_FAILED)))
    //             .get()
    //
    //         globalMutableState._connectionState.value = ConnectionState.CONNECTED
    //         val result = sut.createChannel(channelMock).execute()
    //
    //         result.isSuccess `should be equal to` false
    //         inOrder(channelMock) {
    //             verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
    //             verify(channelMock).syncStatus = SyncStatus.FAILED_PERMANENTLY
    //         }
    //         argumentCaptor<Channel>().apply {
    //             verify(repositoryFacade, times(2)).insertChannel(capture())
    //             with(allValues[1]) {
    //                 syncStatus = SyncStatus.FAILED_PERMANENTLY
    //             }
    //         }
    //     }

    private inner class Fixture {
        private val context: Context = mock()
        private val chatClient: ChatClient = mock {
            on(it.channel(any())) doReturn mock()
            on(it.retryPolicy) doReturn NoRetryPolicy()
        }
        private var user: User = randomUser()
        private var repositoryFacade: RepositoryFacade = mock()

        fun givenUser(user: User) = apply {
            this.user = user
        }

        fun givenRepositoryFacade(repositoryFacade: RepositoryFacade): Fixture = apply {
            this.repositoryFacade = repositoryFacade
        }

        fun givenChatClientResult(result: Result<Channel>): Fixture = apply {
            whenever(chatClient.createChannel(any(), any(), any(), any())) doReturn TestCall(result)
            whenever(chatClient.createChannel(any(), any<List<String>>(), any())) doReturn TestCall(result)
        }

        fun get(): ChatDomainImpl {
            return ChatDomain.Builder(context, chatClient)
                .globalMutableState(globalMutableState)
                .build()
                .let { it as ChatDomainImpl }
                .apply {
                    setUser(this@Fixture.user)
                    userConnected(this@Fixture.user)
                    repos = repositoryFacade
                    scope = testCoroutines.scope
                }
        }
    }

    private fun mockRepositoryFacade() = mock<RepositoryFacade> {
        val user = randomUser()

        on(it.observeLatestUsers()) doReturn MutableStateFlow(mapOf(user.id to user))
    }
}
