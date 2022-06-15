package com.power.http

import androidx.annotation.Keep

/**
 * 作者：Gongsensen
 * 日期：2022/5/18
 * 说明：接口请求返回数据
 */

//@JvmOverloads 可以生成多个构造
@Keep
data class NetRsp(
    var code: String? = "",
    var msg: String? = "",
    var data: Any?,
    var success: Boolean? = false,
)

@Keep
data class NetListRsp @JvmOverloads constructor(
    var code: String? = "",
    var msg: String? = "",
    var data: Any?,
    var success: Boolean? = false,
)