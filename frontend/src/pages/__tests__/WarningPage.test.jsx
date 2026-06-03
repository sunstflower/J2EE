import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import WarningPage from "../WarningPage";
import { clearCurrentUser, saveCurrentUser } from "../../auth";

function mockJsonResponse(data) {
  return Promise.resolve({
    ok: true,
    json: async () => data,
  });
}

describe("WarningPage", () => {
  beforeEach(() => {
    clearCurrentUser();
    saveCurrentUser({ userId: 200, userName: "张药师", role: "PHARMACIST" });
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    clearCurrentUser();
  });

  it("loads low stock and expiry warnings", async () => {
    global.fetch = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              {
                drugId: 1,
                drugCode: "DRUG-LOW",
                drugName: "阿司匹林肠溶片",
                lowStockThreshold: 20,
                availableQuantity: 6,
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 10,
          },
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              {
                inventoryId: 31,
                drugId: 2,
                drugCode: "DRUG-EXP",
                drugName: "维生素C片",
                batchNo: "EXP-01",
                expiryDate: "2026-07-01",
                quantity: 12,
                daysToExpiry: 15,
                warningType: "EXPIRY",
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 10,
          },
        })
      );

    render(<WarningPage />);

    expect(await screen.findByText("阿司匹林肠溶片")).toBeInTheDocument();
    expect(await screen.findByRole("heading", { name: /维生素C片/ })).toBeInTheDocument();
    expect(global.fetch).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/api/warnings/low-stock?pageNum=1&pageSize=10",
      expect.objectContaining({
        headers: expect.any(Headers),
      })
    );
    expect(global.fetch).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/api/warnings/expiry?pageNum=1&pageSize=10&expiryDays=30",
      expect.objectContaining({
        headers: expect.any(Headers),
      })
    );
  });

  it("queries expiry warnings with custom days", async () => {
    const user = userEvent.setup();

    global.fetch = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
          },
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
          },
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              {
                inventoryId: 52,
                drugId: 5,
                drugCode: "DRUG-EXP2",
                drugName: "头孢克肟分散片",
                batchNo: "EXP-02",
                expiryDate: "2026-06-20",
                quantity: 8,
                daysToExpiry: 3,
                warningType: "EXPIRY",
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 10,
          },
        })
      );

    render(<WarningPage />);

    const expiryDaysInput = screen.getByLabelText("临期天数");
    await user.clear(expiryDaysInput);
    await user.type(expiryDaysInput, "7");
    await user.click(screen.getByRole("button", { name: "查询效期预警" }));

    expect(await screen.findByRole("heading", { name: /头孢克肟分散片/ })).toBeInTheDocument();
    expect(global.fetch.mock.calls[2][0]).toBe(
      "http://localhost:8080/api/warnings/expiry?pageNum=1&pageSize=10&expiryDays=7"
    );
  });
});
