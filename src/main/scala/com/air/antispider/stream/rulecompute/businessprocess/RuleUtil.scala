package com.air.antispider.stream.rulecompute.businessprocess

import java.text.SimpleDateFormat
import java.util
import java.util.{Collections, Date}

import com.air.antispider.stream.common.bean.{FlowCollocation, ProcessedData}
import org.joda.time.DateTime

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  *
  */
object RuleUtil {

  /**
    * 时间转换  时间戳 转换成 格式的时间 yyyy-MM-dd HH:mm:ss
    *
    * @param accTimes
    */
  def allTimeList(accTimes: Iterable[String]) = {
    val timeList = new util.ArrayList[Long]()

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    for (time <- accTimes) {
      if (!"".equalsIgnoreCase(time)) {
        val timeStr = new DateTime(time).toDate
        val dt = sdf.format(timeStr)
        val timeL = sdf.parse(dt).getTime
        timeList.add(timeL)
      }
    }
    timeList
  }


  /*

   * 计算关键页面访问时间间隔，单位秒

    *

    * @param accTimes 时间Iterable

   * @return list集合

   */

  def calculateIntervals(accTimes: Iterable[String]): java.util.ArrayList[Int] = {

    val timeList: java.util.List[String] = new java.util.ArrayList[String]

    //格式化时间

    for (time <- accTimes) {

      if (!"0".equalsIgnoreCase(time)) {

        try {
          //  2019-10-10'T'08:14:15+08:00
          //   yyyy-MM-dd'T'HH:mm:ss+08:00  截取到+ 号，并且替换 'T' 为空格
          timeList.add(time.substring(0, time.indexOf("+")).replace("T", " "))

        } catch {

          case e: Exception => e.printStackTrace()

        }

      }

    }


    // 对时间排序
    Collections.sort(timeList)

    val intervals = new java.util.ArrayList[Int]

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    //循环时间

    if (timeList.size() > 1) {

      for (i <- 1 until timeList.size()) {

        //获取间隔时间
        val time1 = sdf.parse(timeList.get(i - 1)).getTime
        val time2 = sdf.parse(timeList.get(i)).getTime

        //计算时间差，毫秒值
        val interval = (time2 - time1) / 1000

        //将时间差封装到list中
        intervals.add(interval.toInt)
      }
    }
    intervals
  }

  /**
    * 从时间间隔获取 最小的时间间隔差
    *
    * @param intervals
    * @return
    */
  def minInterval(intervals: util.ArrayList[Int]): Int = {
    //设置初始时间
    var minInterval = 0
    //  判断
    if (intervals.size() >= 2) {

      minInterval = intervals.get(0)
      // 循环比较
      for (i <- 1 until intervals.size()) {
        // 如果 最小时间间隔 minInterval  大于  intervals.get(i) ，则把 它赋给 minInterval
        if (minInterval > intervals.get(i)) {
          minInterval = intervals.get(i)
        }
      }
    }
    minInterval
  }

  //  def minInterval(list: util.ArrayList[Long]): Int = {
  //    // 计算时间间隔
  //    val intervalLsit = allInterval(list)
  //    // 转成数组，排序
  //    val result = intervalLsit.toArray()
  //    util.Arrays.sort(result)
  //    // 取第一个 最小 的
  //    result(0).toString.toInt
  //
  //  }


  def allInterval(list: util.ArrayList[Long]) = {
    // 排序：因为获取的数据，不能保证顺序性
    val arr = list.toArray
    util.Arrays.sort(arr)
    // 创建list用于封装数据
    val intervalList = new util.ArrayList[Long]()
    // 计算时间差
    if (arr.length > 1) {
      for (i <- 1 until arr.length) {
        val time1 = arr(i - 1).toString.toLong
        val time2 = arr(i).toString.toLong
        val interval = time2 - time1
        intervalList.add(interval)
      }
    }
    intervalList
  }


  /*

   * 小于最短访问间隔（自设）的 关键页面  查询次数

    *

    * @param intervals 集合

   * @return 访问次数

   */

  def calculateMinTimes(intervals: Iterable[String]): Int = {

    //单位时间内最短访问间隔（自设）
    // 预设时间间隔 10秒
    val defaultMinInterval = 10
    var count = 0
    val allTime = allTimeList(intervals)
    val interval = allInterval(allTime)
    if (interval != null && interval.size() > 0) {
      for (i <- 0 until interval.size()) {
        if (interval.get(i) < defaultMinInterval) {
          count = count + 1
        }
      }
    }
    count
  }


  /**
    * 不同UA种类 去重
    */

  def diffUserAgent(agents: Iterable[String]): Int = {

    val set = scala.collection.mutable.Set[String]()
    for (agent <- agents) {
      set.add(agent)
    }
    set.size
    agents.toSet.size
  }


  /*

    * 统计一个IP下查询不同行程的次数

    * @param querys  Iterable

    * @return 集合长度

    */

  def calculateDifferentTripQuerys(querys: Iterable[(String, String)]): Int = {

    //    val list: java.util.List[String] = new java.util.ArrayList[String]
    //    for (query <- querys) {
    //      list.add(query._1 + "-->" + query._2)
    //    }
    //    val hashSet = new util.HashSet(list)
    //    list.clear()
    //    list.addAll(hashSet)
    //    list.size()

    val set = scala.collection.mutable.Set[String]()
    for (query <- querys) {
      set.add(query._1 + "-->" + query._2)
    }
    set.size
  }


  /*

     * 不同种类的cookies

      * @param cookies cookies

     * @return 集合长度

     */

  def cookiesCounts(cookies: Iterable[String]): Int = {

    val list: java.util.List[String] = new java.util.ArrayList[String]

    for (cookie <- cookies) {

      if (!"".equals(cookie)) {

        list.add(cookie)

      }

    }

    val hashSet = new util.HashSet(list)

    list.clear()

    list.addAll(hashSet)

    list.size().toString.toInt

    //老师的写法    cookies.toSet.size
  }


  /*

   * 取到每条记录对应的统计结果

    *

    * @param processedData processedData

   * @param FlowCollocations 流程Collocations

   * @param ip ip

   * @param request 请求

   * @param ipBlockAccessCountsMap IP段访问量

   * @param ipAccessCountsMap 单位时间内某个IP访问量

   * @param criticalPagesCountsMap 单位时间内的关键页面访问总量

   * @param userAgentCountsMap 单位时间内的UA种类数统计

   * @param criticalMinIntervalMap 单位时间内的关键页面最短访问间隔

   * @param accessIntervalLessThanDefaultMap 单位时间内小于最短访问间隔（自设）的关键页面查询次数

   * @param differentTripQuerysMap 单位时间内查询不同行程的次数

   * @param criticalCookiesMap 单位时间内关键页面的Cookie数


   * @return AntiCalculateResult流程计算结果

   */

  def calculateAntiResult(processedData: ProcessedData,

                          FlowCollocations: Array[FlowCollocation],

                          ip: String, request: String,

                          ipBlockAccessCountsMap: scala.collection.Map[String, Int],

                          ipAccessCountsMap: scala.collection.Map[String, Int],

                          criticalPagesCountsMap: scala.collection.Map[String, Int],

                          userAgentCountsMap: scala.collection.Map[String, Int],

                          criticalMinIntervalMap: scala.collection.Map[String, Int],

                          accessIntervalLessThanDefaultMap: scala.collection.Map[(String, String), Int],

                          differentTripQuerysMap: scala.collection.Map[String, Int],

                          criticalCookiesMap: scala.collection.Map[String, Int]

                         ): AntiCalculateResult = {


    //当前处理这个ip的段

    val index = ip.indexOf(".")

    val ipBlock = try {

      ip.substring(0, ip.indexOf(".", index + 1))

    } catch {

      case e: Exception => ""

    }

    //IP段访问量
    val ipBlockCounts = ipBlockAccessCountsMap.getOrElse(ipBlock, 0)

    //这条记录对应的单位时间访问量
    val ipAccessCounts = ipAccessCountsMap.getOrElse(ip, 0)

    //这条记录对应的单位时间内的关键页面访问总量
    val criticalPageAccessCounts = criticalPagesCountsMap.getOrElse(ip, 0)

    //这条记录对应的单位时间内的UA种类数统计
    val userAgentCounts = userAgentCountsMap.getOrElse(ip, 0)

    //这条记录对应的单位时间内的关键页面最短访问间隔
    val critivalPageMinInterval = criticalMinIntervalMap.getOrElse(ip, 0)

    //这条记录对应的单位时间内小于最短访问间隔（自设）的关键页面查询次数
    val accessPageIntervalLessThanDefault = accessIntervalLessThanDefaultMap.getOrElse((ip, request), 0)

    //这条记录对应的单位时间内查询不同行程的次数
    val differentTripQuerysCounts = differentTripQuerysMap.getOrElse(ip, 0)

    //这条记录对应的单位时间内关键页面的Cookie数
    val criticalCookies = criticalCookiesMap.getOrElse(ip, 0)


    //这条记录对应的所有标签封装到map中  写法报错
    val paramMap = scala.collection.mutable.Map[String, Int]()
    paramMap += ("ipBlock" -> ipBlockCounts)
    paramMap += ("ip" -> ipAccessCounts)
    paramMap += ("criticalPages" -> criticalPageAccessCounts)
    paramMap += ("userAgent" -> userAgentCounts)
    paramMap += ("criticalPagesAccTime" -> critivalPageMinInterval)
    paramMap += ("flightQuery" -> differentTripQuerysCounts)
    paramMap += ("criticalCookies" -> criticalCookies)
    paramMap += ("criticalPagesLessThanDefault" -> accessPageIntervalLessThanDefault)


    //    val paramMap = scala.collection.mutable.Map[String, Int](
    //      "ipBlock" -> ipBlockCounts,
    //      "ip" -> ipAccessCounts,
    //      "criticalPages" -> criticalPageAccessCounts,
    //      "userAgent" -> userAgentCounts,
    //      "criticalPagesAccTime" -> critivalPageMinInterval,
    //      "flightQuery" -> differentTripQuerysCounts,
    //      "criticalCookies" -> criticalCookies,
    //      "criticalPagesLessThanDefault" -> accessPageIntervalLessThanDefault
    //    )


    /*
      * 计算打分结果

      * paramMap：在5分钟之内统计的结果

      * FlowCollocations：数据库规则，规定5分钟内不允许超过限制的值

      * 最终结果为：Array[（流程Id，流程得分，流程阈值,是否大于阈值大于阈值定义为爬虫）]

      */

    val flowsScore: Array[FlowScoreResult] = calculateFlowsScore(paramMap, FlowCollocations)

    //针对这条记录封装的打分类，包含了这条记录的所有统计结果、打分、是否命中等等

    AntiCalculateResult(
      processedData,
      ip,

      ipBlockCounts,
      ipAccessCounts,
      criticalPageAccessCounts,
      userAgentCounts,

      critivalPageMinInterval,
      accessPageIntervalLessThanDefault,
      differentTripQuerysCounts,
      criticalCookies,

      flowsScore)

  }


  /*

   * 计算规则得分

   *

    * @param paramMap paramMap

   * @param flowList 流程列表

   * @return 流程得分

   */

  def calculateFlowsScore(paramMap: scala.collection.mutable.Map[String, Int],
                          flowList: Array[FlowCollocation]): Array[FlowScoreResult] = {

    //封装最终打分结果：flowId、flowScore、flowLimitedScore、是否超过阈值、flowStrategyCode、命中规则列表、命中时间
    val flowScores = new ArrayBuffer[FlowScoreResult]

    //循环数据库查询出来的所有流程，进行匹配打分

    for (flow <- flowList) {

      //拿出当前流程的规则列表，就是我们web页面配置的那些阈值
      val ruleList = flow.rules

      //用来封装命中的规则的rileId
      val hitRules = ListBuffer[String]()

      //保存规则计算结果的二维数组（2行，n列），第一维是之前streaming计算统计的结果，
      // 第二维是针对对应统计结果的数据库打分结果


      val result = Array.ofDim[Double](2,ruleList.size)



      //根据每个流程对应的规则统计结果与预设的规则进行对比，若统计结果大于预设值，则对应的规则得分有效，否则，无效（即设为0）
      var ruleIndex = 0

      //规则是否触发，也就是web页面的复选框有没有被勾选
      val isTriggered = new ArrayBuffer[Int]


      //循环数据库规则，循环结束，会将result填满，hitRules填满，isTriggered填满
      for (rule <- ruleList) {

        //规则状态放到这个数组
        isTriggered += rule.ruleStatus

        //规则名字
        val ruleName = rule.ruleName

        //通过规则名字去streaming统计的结果中找数值
        val ruleValue = paramMap.getOrElse(ruleName, 0)

        //把streaming统计结果封装到第0行，第ruleIndex列，后续ruleIndex会做+1操作
        result(0)(ruleIndex) = ruleValue


        //拿出数据库对应这个 规则设置的阈值，自己设置的值
        val ruleValue1 = if ("accessPageIntervalLessThanDefault".equals(ruleName)) {
          rule.ruleValue1
        } else {
          rule.ruleValue0
        }

        //数据库对应这个规则的打分
        val ruleScore = rule.ruleScore

        if (ruleValue > ruleValue1) {

          //如果streaming统计结果超过了数据库阈值，将打分记录到result的第1行，
          // 第ruleIndex列，后续ruleIndex会做+1操作

          result(1)(ruleIndex) = ruleScore

          //打分放进去，则规则命中，将规则信息添加到数组
          hitRules.append(rule.ruleId)


        } else {

          //没命中，打分设置为0

          result(1)(ruleIndex) = 0

        }

        //ruleIndex做+1 操作，继续对比第二个rule规则，让其自增
        ruleIndex = ruleIndex + 1

      }



      // todo 计算流程打分，打分区间为：平均分--10*平均分

      val flowScore = calculateFlowScore(result, isTriggered.toArray)

      //命中时间
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val hitTime = sdf.format(new Date())

      //（流程Id，流程得分，流程阈值,是否大于阈值，strategyCode，命中规则id列表，命中时间）- 大于阈值定义为爬虫

      flowScores.append(

        FlowScoreResult(
          flow.flowId,
          flowScore,
          flow.flowLimitScore,
          flowScore > flow.flowLimitScore,
          flow.strategyCode,
          hitRules.toList,
          hitTime)
      )

    }

    //将所有流程的结果信息返回

    flowScores.toArray

  }


  /*

   * 计算流程得分-请参考详细设计说明书（规则打分，流程计算）及对应的原型设计（流程管理）

    * 系数2权重：60%，数据区间：10-60

    * 系数3权重：40，数据区间：0-40

    * 系数2+系数3区间为：10-100

    * 系数1为:平均分/10

    * 所以，factor1 * (factor2 + factor3)区间为:平均分--10倍平均分

    * @param result  result二维数组

   * @param isTriggered isTriggered数组

   * @return 规则得分

   */


  def calculateFlowScore(result: Array[Array[Double]],
                         isTriggered: Array[Int]): Double = {

    //打分列表
    val scores = result(1)

    //总打分
    val sum = scores.sum

    //打分列表长度
    val dim = scores.length

    //系数1：平均分/10
    val factor1 = sum / (10 * dim)

    //命中数据库开放规则的score
    val xa = triggeredScore(scores, isTriggered)

    //命中规则中，规则分数最高的
    val maxInXa = if (xa.isEmpty) {

      0.0

    } else {

      xa.max

    }

    //系数2：系数2的权重是60，指的是最高score以6为分界，最高score大于6，就给满权重60，不足6，就给对应的maxInXa*10

    val factor2 = if (1 < (1.0 / 6.0) * maxInXa) {

      60

    } else {

      (1.0 / 6.0) * maxInXa * 60

    }

    //系数3：打开的规则总分占总规则总分的百分比，并且系数3的权重是40

    val factor3 = 40 * (xa.sum / sum)

    /*

    * 系数2权重：60%，数据区间：10-60

    * 系数3权重：40，数据区间：0-40

    * 系数2+系数3区间为：10-100

    * 系数1为:平均分/10

    * 所以，factor1 * (factor2 + factor3)区间为:平均分--10倍平均分

      */

    factor1 * (factor2 + factor3)
  }

  /**
    *
    * @param scores
    * @param isTriggered
    */
  def triggeredScore(scores: Array[Double], isTriggered: Array[Int]) = {

    // 创建 ArrayBuffer
    val arrayBuffer = new ArrayBuffer [Double]()

    // 判断
  for(i <- 0 until isTriggered.length){
    // 判断
    if(isTriggered(i) == 0){
      arrayBuffer += scores(i)
    }
  }
    arrayBuffer.toArray
  }


}
