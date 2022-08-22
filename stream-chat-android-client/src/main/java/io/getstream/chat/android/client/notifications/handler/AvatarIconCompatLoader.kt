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

package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmapOrNull
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Create [IconCompat] for a given user to be shown on notifications.
 */
public interface AvatarIconCompatLoader {

    /**
     * Create an [IconCompat] for a given user or null if it couldn't be created.
     *
     * @param user from which the [IconCompat] should be created.
     *
     * @return an [IconCompat] for the given user or null.
     */
    public suspend fun loadAvatar(user: User): IconCompat?
}

/**
 * Default implementation of [AvatarIconCompatLoader].
 */
internal class DefaultAvatarIconCompatLoader(val context: Context) : AvatarIconCompatLoader {
    override suspend fun loadAvatar(user: User): IconCompat? =
        user.image
            .takeUnless { it.isEmpty() }
            ?.let {
                withContext(DispatcherProvider.IO) {
                    runCatching {
                        URL(it).openStream().use {
                            RoundedBitmapDrawableFactory.create(
                                context.resources,
                                BitmapFactory.decodeStream(it),
                            )
                                .apply { isCircular = true }
                                .toBitmapOrNull()
                        }
                            ?.let(IconCompat::createWithBitmap)
                    }.getOrNull()
                }
            }
}
