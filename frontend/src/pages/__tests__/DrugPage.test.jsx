import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import DrugPage from "../DrugPage";
import { clearCurrentUser, saveCurrentUser } from "../../auth";

function mockJsonResponse(data) {
  return Promise.resolve({
    ok: true,
    json: async () => data,
  });
}

describe("DrugPage", () => {
  beforeEach(() => {
    clearCurrentUser();
    saveCurrentUser({ userId: 100, userName: "王医生", role: "DOCTOR" });
    global.fetch = vi.fn(() =>
      mockJsonResponse({
        code: 0,
        message: "success",
        data: {
          records: [
            {
              id: 1,
              drugCode: "DRUG-001",
              drugName: "阿莫西林胶囊",
              category: "抗生素",
              unit: "盒",
              purchasePrice: 12.5,
              salePrice: 18.8,
              lowStockThreshold: 20,
              enabled: 1,
            },
          ],
          total: 1,
          pageNum: 1,
          pageSize: 10,
        },
      })
    );
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    clearCurrentUser();
  });

  it("loads drugs with current user headers", async () => {
    render(<DrugPage />);

    expect(await screen.findByText("阿莫西林胶囊")).toBeInTheDocument();

    expect(global.fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/drugs?pageNum=1&pageSize=10",
      expect.objectContaining({
        headers: expect.any(Headers),
      })
    );

    const requestOptions = global.fetch.mock.calls[0][1];
    expect(requestOptions.headers.get("X-User-Id")).toBe("100");
    expect(requestOptions.headers.get("X-User-Name")).toBe(encodeURIComponent("王医生"));
    expect(requestOptions.headers.get("X-User-Role")).toBe("DOCTOR");
  });

  it("creates a drug and refreshes list", async () => {
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
          data: 101,
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              {
                id: 101,
                drugCode: "DRUG-NEW",
                drugName: "布洛芬缓释胶囊",
                category: "止痛",
                unit: "盒",
                purchasePrice: 10,
                salePrice: 15,
                lowStockThreshold: 8,
                enabled: 1,
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 10,
          },
        })
      );

    render(<DrugPage />);

    await user.type(screen.getByLabelText("药品编码"), "DRUG-NEW");
    await user.type(screen.getByLabelText("药品名称"), "布洛芬缓释胶囊");
    await user.type(screen.getByLabelText("分类"), "止痛");
    await user.clear(screen.getByLabelText("采购价"));
    await user.type(screen.getByLabelText("采购价"), "10");
    await user.clear(screen.getByLabelText("销售价"));
    await user.type(screen.getByLabelText("销售价"), "15");
    await user.clear(screen.getByLabelText("最低库存阈值"));
    await user.type(screen.getByLabelText("最低库存阈值"), "8");
    await user.click(screen.getByRole("button", { name: "新增药品" }));

    await waitFor(() => {
      expect(screen.getByText("药品新增成功，列表已刷新。")).toBeInTheDocument();
    });

    expect(global.fetch).toHaveBeenCalledTimes(3);
    expect(global.fetch.mock.calls[1][0]).toBe("http://localhost:8080/api/drugs");
    expect(global.fetch.mock.calls[1][1].method).toBe("POST");
    expect(global.fetch.mock.calls[1][1].body).toContain("\"drugCode\":\"DRUG-NEW\"");
    expect(await screen.findByText("布洛芬缓释胶囊")).toBeInTheDocument();
  });

  it("confirms before deleting a drug", async () => {
    const user = userEvent.setup();
    const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

    render(<DrugPage />);

    await user.click(await screen.findByRole("button", { name: "删除" }));

    await waitFor(() => {
      expect(confirmSpy).toHaveBeenCalledWith("确认删除该药品吗？");
    });
  });
});
