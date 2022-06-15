package com.power.land.sample

import com.power.base.utils.JobExecutor
import java.util.concurrent.Future

/**
 * 作者：Gongsensen
 * 日期：2022/6/15
 * 说明：JobExecutor异步任务执行器示例
 */
class JobExecutorSample {
    private var future: Future<String>? = null

    fun jobTest() {
        future?.cancel(true)
        future = JobExecutor.instance().submit(object : JobExecutor.Task<String> {
            override fun call(): String {
                //在此异步执行耗时任务，并返回值
                return mockNetData()
            }

            override fun onMainThread(result: String?) {
                super.onMainThread(result)
                //回到主线程处理返回数据
                //do something...
            }

        })
    }

    fun mockNetData(): String {
        return "模拟网络数据返回"
    }


}