package controllers


import java.io.File
import java.nio.file.Paths
import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.google.common.annotations.VisibleForTesting
import entity.{Artical, User}
import play.api._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import util.JSONUtil


/**
  * Created by zhuleqi on 2017/6/10.
  */
class ArticalController  @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  def collection = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("artical"))

  def showEditor()=Action{
    Ok(views.html.artical())
  }

  def showArticals()=Action{
    Ok(views.html.articals())
  }

  def insert(title:String,content:String)=Action.async{
    val userId = "123"
    val artical = Artical(
      UUID.randomUUID().toString,
      title,
      content,
      userId,
      "",
      "",
      System.currentTimeMillis(),
      System.currentTimeMillis()
    )

    collection.flatMap(_.insert(artical)).map(wr=>{
      Ok(JSONUtil.toSuccessJSON())
    })
  }

  /**
    * 根据文章ID获取文章
    * @param id  文章Id
    * @return 返回根据ID找到的文章
    */
  def find(id:String)=Action.async{
    collection.flatMap(_.find(Json.obj({"_id"->id})).cursor[Artical]().collect[List]()).map(arts=>Ok(JSONUtil.toJSON(arts.head)))
  }

  /**
    * 文章列表分页获取文章
    * @param page
    * @param pageSize
    * @return
    */
  def findAll(page:Int,pageSize:Int)=Action.async{
    val startIndex = pageSize*(page-1);
    val endIndex = pageSize*page
    val queryOpts = QueryOpts(skipN = startIndex,batchSizeN = pageSize)
    collection.flatMap(_.find(Json.obj())
          .options(queryOpts)
          .cursor[Artical]()
          .collect[List](pageSize))
      .map(articals=>Ok(JSONUtil.toJSON(page,pageSize,articals)))
  }

  def upload()=Action(parse.multipartFormData){ request=>{
    request.body.file("audio").map( audio=>{
      val fileName = audio.filename
      val contentType = audio.contentType
      val toFile = new File(s"/tmp/$fileName")
      toFile.createNewFile()
      audio.ref.moveTo(toFile,true)
      Ok(JSONUtil.toSuccessJSON())
    }

    ).getOrElse(
      Ok(JSONUtil.toErrorJSON())
    )
  }
  }
}
