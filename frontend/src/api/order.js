import request from '../utils/request'

export function createOrder(data) {
  return request.post('/order/shopOrder/create', data)
}

export function getMyOrderPage(params) {
  return request.get('/order/shopOrder/myPage', { params })
}

export function getOrderPage(params) {
  return request.get('/order/shopOrder/page', { params })
}

export function getOrderDetail(id) {
  return request.get(`/order/shopOrder/${id}`)
}

export function cancelOrder(id, reason) {
  return request.put(`/order/shopOrder/${id}/cancel`, null, { params: { reason } })
}

export function payOrder(id) {
  return request.put(`/order/shopOrder/${id}/pay`)
}

export function deliverOrder(id) {
  return request.put(`/order/shopOrder/${id}/deliver`)
}

export function receiveOrder(id) {
  return request.put(`/order/shopOrder/${id}/receive`)
}