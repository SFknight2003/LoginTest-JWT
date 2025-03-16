import axios from "axios";
import {ElMessage} from "element-plus";

// 统一存储token信息时的固定key值
const authItemName = "access_token"

// 默认请求失败返回信息
const defaultFailure = (message, code, url) => {
    console.warn(`请求地址：${url}，状态码：${code}，错误信息：${message}`)
    // 发送错误警告信息
    ElMessage.warning(message)
}

// 默认报错返回信息
const  defaultError = (err) => {
    console.error(err)
    // 发送错误警告信息
    ElMessage.error(`发生了错误，请联系管理员`)
}

// 拿取token
function takeAccessToken(){
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if (!str) return null;      // 没有存储token
    const authObj = JSON.parse(str)     // str转换为JSON
    // 超出过期时间，删除token
    if(authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning(`登录状态已过期，请重新登录`)
        return null
    }
    return authObj.token
}
// 删除token
function  deleteAccessToken(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

// 封装请求头
function accessHeader(){
    const token = takeAccessToken();
    return token ? {'Authorization' : `Bearer ${takeAccessToken()}`} : {}     // 拿到token后进行组合
}

/**
 * 存储token
 * 存储方案分为session和localstorage，后者为‘记住我’存储方式
 * @param remember  记住我
 * @param token   token
 * @param expire  过期时间
 */
function storeAccessToken(remember, token, expire){
    const authObj = { token:token, expire:expire }
    const str = JSON.stringify(authObj)  // JSON转成字符串
    if(remember){
        localStorage.setItem(authItemName,str)  // 将用户token和和过期时间，存储在value中
    } else {
        sessionStorage.setItem(authItemName,str)
    }
}

/**
 * 内部使用的post请求
 */
function  internalPost(url, data, headers, success, failure, error=defaultError){
    axios.post(url, data,{headers: headers}).then(({data}) => {
        if ( data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

/**
 * 内部使用的get方法
 */
function  internalGet(url, headers, success, failure, error=defaultError){
    axios.get(url,{headers: headers}).then(({data}) => {
        if ( data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

// 暴露给外部的get和post请求
function get(url, success, failure = defaultFailure){
    internalGet(url, accessHeader(), success, failure)
}

function post(url, data, success, failure = defaultFailure){
    internalPost(url, data, accessHeader(), success, failure)
}

/**
 * login登录方法
 */
function login(username, password, remember, success, failure = defaultFailure){
    // 调用内部Post请求
    internalPost(
        '/api/auth/login',
        {
            username: username,
            password: password
        },
        {
            'Content-Type':'application/x-www-form-urlencoded'
        },
        (data)=>{  // 此处data是internalPost调用传入的data，success(data)
            storeAccessToken(remember,data.token,data.expire)    // 存储token
            ElMessage.success(`登录成功，欢迎${data.username}来到我们系统`)
            success(data)
        }, failure)
}

/**
 * 退出登录
 * @param success
 * @param failure
 */
function logout(success, failure = defaultFailure){
    get('/api/auth/logout',()=>{
        deleteAccessToken()
        ElMessage.success('退出登录成功，欢迎再次使用')
        success()   // 调用回调函数跳转welcome页面
    },failure)
}

// 是否登录
function unauthorized(){
    return !takeAccessToken()
}

// 暴露方法
export {login, logout, get, post, unauthorized}

