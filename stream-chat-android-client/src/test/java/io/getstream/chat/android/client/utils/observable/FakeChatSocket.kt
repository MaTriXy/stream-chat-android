/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener

internal class FakeChatSocket : ChatSocket {

    private val listeners = mutableSetOf<SocketListener>()

    override fun connect(user: User) {
    }

    override fun connectAnonymously() {
    }

    override fun addListener(listener: SocketListener) {
        listeners += listener
    }

    override fun removeListener(listener: SocketListener) {
        listeners -= listener
    }

    fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun disconnect() {
    }

    override fun releaseConnection() {
    }
}
