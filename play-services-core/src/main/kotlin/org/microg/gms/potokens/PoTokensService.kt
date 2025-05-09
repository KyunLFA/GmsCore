/*
 * SPDX-FileCopyrightText: 2024 microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.potokens

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.Feature
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.api.internal.IStatusCallback
import com.google.android.gms.common.internal.ConnectionInfo
import com.google.android.gms.common.internal.GetServiceRequest
import com.google.android.gms.common.internal.IGmsCallbacks
import com.google.android.gms.potokens.PoToken
import com.google.android.gms.potokens.internal.IPoTokensService
import com.google.android.gms.potokens.internal.ITokenCallbacks
import org.microg.gms.BaseService
import org.microg.gms.common.GmsService
import org.microg.gms.profile.ProfileManager

private val FEATURES = arrayOf(Feature("PO_TOKENS", 1))

class PoTokensService : BaseService(TAG, GmsService.POTOKENS) {

    override fun handleServiceRequest(callback: IGmsCallbacks, request: GetServiceRequest, service: GmsService) {
        Log.d(TAG, "PoTokensApiService handleServiceRequest")
        ProfileManager.ensureInitialized(this)
        callback.onPostInitCompleteWithConnectionInfo(
            CommonStatusCodes.SUCCESS,
            PoTokensServiceImpl(request.packageName, PoTokenHelper(this), lifecycle),
            ConnectionInfo().apply { features = FEATURES }
        )
    }
}


class PoTokensServiceImpl(
    private val packageName: String,
    private val helper: PoTokenHelper,
    override val lifecycle: Lifecycle
) : IPoTokensService.Stub(), LifecycleOwner {

    override fun responseStatus(call: IStatusCallback, code: Int) {
        Log.e(TAG, "responseStatus success")
        call.onResult(Status.SUCCESS)
    }

    override fun responseStatusToken(call: ITokenCallbacks, i: Int, bArr: ByteArray) {
        Log.d(TAG, "responseStatusToken packageName: $packageName")
        lifecycleScope.launchWhenStarted {
            Log.d(TAG, "responseStatusToken start")
            runCatching {
                val bytes = helper.callPoToken(packageName, bArr)
                Log.d(TAG, "responseStatusToken result: ${bytes.size}")
                call.responseToken(Status.SUCCESS, PoToken(bytes))
            }
            Log.d(TAG, "responseStatusToken end")
        }
    }

}
