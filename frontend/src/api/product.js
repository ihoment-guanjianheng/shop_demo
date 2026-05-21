import request from '../utils/request'
import axios from 'axios'

export function getProductPage(params) {
  return request.get('/product/page', { params })
}

export function getMyProductPage(params) {
  return request.get('/product/myPage', { params })
}

export function getProductDetail(id) {
  return request.get(`/product/${id}`)
}

export function addProduct(data) {
  return request.post('/product/add', data)
}

export function updateProduct(id, data) {
  return request.put(`/product/${id}`, data)
}

export function updateProductStatus(id, status) {
  return request.put(`/product/${id}/status`, null, { params: { status } })
}

export function exportProduct() {
  return request.get('/product/export')
}

export function addSku(data) {
  return request.post('/product/sku/add', data)
}

export function updateSku(id, data) {
  return request.put(`/product/sku/${id}`, data)
}

export function deleteSku(id) {
  return request.delete(`/product/sku/${id}`)
}

export function updateSkuStock(id, delta) {
  return request.put(`/product/sku/${id}/stock`, null, { params: { delta } })
}

export function getImagePresign(filename) {
  return request.get('/product/image/presign', { params: { filename } })
}

export function uploadToPresign(url, file, contentType) {
  return axios.put(url, file, {
    headers: { 'Content-Type': contentType || file.type }
  })
}