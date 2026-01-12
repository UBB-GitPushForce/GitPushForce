// src/services/receipt-service.ts
import apiClient from './api-client'; // Import your configured client

export interface ReceiptItem {
  name: string;
  quantity: number;
  price: number;
  category: string;
  keywords: string[];
}

export interface ReceiptAnalysisResult {
  items: ReceiptItem[];
  total: number;
}

const processReceipt = async (file: File): Promise<ReceiptAnalysisResult> => {
  const formData = new FormData();
  formData.append('image', file); 

  const response = await apiClient.post<ReceiptAnalysisResult>('/receipt/process-receipt', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export default {
  processReceipt
};