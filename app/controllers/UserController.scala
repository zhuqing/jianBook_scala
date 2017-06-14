package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import entity.User
import play.api._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONDocument

 import reactivemongo.play.json._
import reactivemongo.play.json.collection._

/**
  * Created by zhuleqi on 2017/3/22.
  */
class UserController @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  /**
    * 获取mongoDb的collection
    * @return
    */
  def userCollection = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("user"))

  def regist()=Action{
    Ok(views.html.userRegist("用户注册"))
  }

  /**
    *插入用户
    * @param name
    * @param passowrd
    * @param email
    * @return
    */
  def insert(name:String,passowrd:String,email:String)=Action.async{

    var user = User(UUID.randomUUID().toString,
      name,
      passowrd,
      email,
      System.currentTimeMillis(),
      System.currentTimeMillis()
    )
  userCollection.flatMap(_.insert(user)).map(wr =>Ok(Json.toJson(user).toString()))


  }

  /**
    * 更新User的Email
    * @param id
    * @param email
    * @return
    */
  def update(id:String,email:String) = Action.async{
    userCollection.flatMap(_.update(Json.obj("_id"->id),Json.obj("$set"->Json.obj("email"->email)))).map(ss=>{
      Ok("ok")
    })
  }

  /**
    *根据ID查找用户
    * @param id
    * @return
    */
  def find(id:String)=Action.async{
   userCollection.flatMap(_.find(Json.obj("_id"->id)).cursor[User]().collect[List]()).map(users=>{
      Ok(Json.toJson(users.head).toString());
   })
  }

  /**
    * 删除用户
    * @param id
    * @return
    */
  def remove(id:String) =Action.async{
    userCollection.flatMap(_.remove(Json.obj("_id"->id))).map(findMS =>{
      Ok("delete ok")
    })
  }

  /**
    * 分页查找用户列表
    * @param page
    * @param pageSize
    * @return
    */

  def findAll(page:Int,pageSize:Int) = Action.async{
      val startIndex = pageSize*(page-1);
      val endIndex = pageSize*page
    
      userCollection.flatMap(_.find(Json.obj()).options(QueryOpts(skipN =startIndex ,batchSizeN = pageSize)).cursor[User]().collect[List](pageSize)).map(users=>{
        //生成JsonList
        Ok(Json.obj("values"->users.map(Json.toJson(_))))
      })
  }

  def login(name:String,password:String) = Action{
    Ok("")
  }
}
