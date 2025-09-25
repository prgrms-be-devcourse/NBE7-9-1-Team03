import type { CartItem } from "./product";

export type OrderRequest = {
  email: string;
  // address: string;
  // zipcode: string;
  items: Array<{
    productId: number;
    quantity: number;
  }>;
};

export type OrderResponse = {
  msg: string;
  data?: any;
};
