export type Session = {
  token: string;
  username: string;
  role: string;
};

export type DailySales = {
  day: string;
  totalMinor: number;
  orderCount: number;
};

export type OrderLine = {
  barcode: string;
  productName: string;
  quantity: number;
  unitPriceMinor: number;
};

export type Order = {
  id: string;
  marketplace: string;
  externalOrderId: string;
  customerName: string;
  orderedAt: string;
  totalMinor: number;
  lines: OrderLine[];
};

export type LowStock = {
  barcode: string;
  name: string;
  quantity: number;
  threshold: number;
  salePriceMinor: number;
};

export type Failure = {
  id: string;
  marketplace: string;
  operation: string;
  externalId: string | null;
  errorMessage: string;
  createdAt: string;
  retryTopic: string;
};

export type Question = {
  id: string;
  marketplace: string;
  externalQuestionId: string;
  productBarcode: string;
  customerName: string;
  text: string;
  askedAt: string;
  status: string;
  draftText: string | null;
};

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }
}

export async function api<T>(path: string, token: string | null, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(path, { ...init, headers });
  if (!response.ok) {
    let message = response.statusText;
    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) {
        message = body.message;
      }
    } catch {
      // The error body is not JSON.
    }
    throw new ApiError(message, response.status);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function money(minor: number): string {
  return new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(minor / 100);
}

export function when(value: string): string {
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}
