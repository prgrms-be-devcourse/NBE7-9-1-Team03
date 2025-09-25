import type { CartItem } from "./product";

// 단일 주문 처리
export type SingleOrder = {
  customerEmail: string;
  productId: number;
  quantity: number;
};
// 단일 주문을 모아서 한 번에 처리하기 위한 리스트
export type OrderRequest = SingleOrder[];

export type OrderResponse = {
  msg: string;
  data?: any;
};