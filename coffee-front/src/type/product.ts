export type ProductDto = {
    id: number;
    name: string;
    price: number;
    stock: number;
    imageUrl?: string;
  };
  
  export type CartItem = {
    productId: number;
    name: string;
    price: number;
    quantity: number;
  };
  
  export type CreateProductDto = {
    name: string;
    price: number;
    stock: number;
    imageUrl?: string;
  }

  export type UpdateProductDto = {
    name: string;
    price: number;
    stock: number;
    imageUrl?: string;
  }

  export type FormState = {
    name: string;
    price: string;
    stock: string;
    imageUrl: string;
  }