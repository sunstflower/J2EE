import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import InventoryPage from "../InventoryPage";
import { clearCurrentUser, saveCurrentUser } from "../../auth";

function mockJsonResponse(data) {
  return Promise.resolve({
    ok: true,
    json: async () => data,
  });
}

function buildInventoryPageData() {
  return {
    code: 0,
    message: "success",
    data: {
      records: [
        {
          id: 10,
          drugId: 1,
          drugCode: "DRUG-001",
          drugName: "阿莫西林胶囊",
          batchNo: "BATCH-01",
          expiryDate: "2026-12-31",
          quantity: 30,
          lockedQuantity: 0,
          locationCode: "A-01",
        },
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10,
    },
  };
}

function buildRecordPageData() {
  return {
    code: 0,
    message: "success",
    data: {
      records: [
        {
          id: 100,
          drugId: 1,
          drugCode: "DRUG-001",
          drugName: "阿莫西林胶囊",
          inventoryId: 10,
          batchNo: "BATCH-01",
          recordType: "INBOUND",
          quantityChange: 20,
          beforeQuantity: 10,
          afterQuantity: 30,
          bizNo: "IN-100",
          operatorName: "张药师",
        },
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10,
    },
  };
}

describe("InventoryPage", () => {
  beforeEach(() => {
    clearCurrentUser();
    saveCurrentUser({ userId: 200, userName: "张药师", role: "PHARMACIST" });
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    clearCurrentUser();
  });

  it("loads inventories and records with current user headers", async () => {
    global.fetch = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse(buildInventoryPageData()))
      .mockImplementationOnce(() => mockJsonResponse(buildRecordPageData()));

    render(<InventoryPage />);

    expect(await screen.findByText("库存模块联调")).toBeInTheDocument();
    expect(await screen.findByText("阿莫西林胶囊")).toBeInTheDocument();
    expect(await screen.findByText("INBOUND")).toBeInTheDocument();

    expect(global.fetch).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/api/inventories?pageNum=1&pageSize=10",
      expect.objectContaining({
        headers: expect.any(Headers),
      })
    );
    expect(global.fetch).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/api/inventories/records?pageNum=1&pageSize=10",
      expect.objectContaining({
        headers: expect.any(Headers),
      })
    );

    const requestOptions = global.fetch.mock.calls[0][1];
    expect(requestOptions.headers.get("X-User-Id")).toBe("200");
    expect(requestOptions.headers.get("X-User-Name")).toBe(encodeURIComponent("张药师"));
    expect(requestOptions.headers.get("X-User-Role")).toBe("PHARMACIST");
  });

  it("submits inbound request with operator name from current user", async () => {
    const user = userEvent.setup();

    global.fetch = vi
      .fn()
      .mockImplementationOnce(() => mockJsonResponse(buildInventoryPageData()))
      .mockImplementationOnce(() => mockJsonResponse(buildRecordPageData()))
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: 11,
        })
      )
      .mockImplementationOnce(() => mockJsonResponse(buildInventoryPageData()))
      .mockImplementationOnce(() => mockJsonResponse(buildRecordPageData()));

    render(<InventoryPage />);

    const inboundForm = screen.getByRole("heading", { name: "库存入库" }).closest("form");
    const scoped = within(inboundForm);

    await user.type(scoped.getByLabelText("药品 ID"), "1");
    await user.type(scoped.getByLabelText("批次号"), "BATCH-NEW");
    await user.type(scoped.getByLabelText("有效期"), "2026-12-31");
    await user.clear(scoped.getByLabelText("数量"));
    await user.type(scoped.getByLabelText("数量"), "20");
    await user.type(scoped.getByLabelText("库位"), "A-02");
    await user.type(scoped.getByLabelText("业务单号"), "IN-200");
    await user.type(scoped.getByLabelText("备注"), "演示入库");
    await user.click(scoped.getByRole("button", { name: "提交入库" }));

    await waitFor(() => {
      expect(screen.getByText("入库成功，库存和流水已刷新。")).toBeInTheDocument();
    });

    expect(global.fetch).toHaveBeenCalledTimes(5);
    expect(global.fetch.mock.calls[2][0]).toBe("http://localhost:8080/api/inventories/inbound");
    expect(global.fetch.mock.calls[2][1].method).toBe("POST");
    expect(global.fetch.mock.calls[2][1].body).toContain("\"operatorName\":\"张药师\"");
    expect(global.fetch.mock.calls[2][1].body).toContain("\"bizNo\":\"IN-200\"");
  });
});
