export type ProductDto = {
    id: number;
    name: string;
    price: number;
    imageUrl?: string;
  };
  
  export type CartItem = {
    productId: number;
    name: string;
    price: number;
    quantity: number;
  };
  