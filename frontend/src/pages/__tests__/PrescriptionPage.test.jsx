import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import PrescriptionPage from "../PrescriptionPage";
import { clearCurrentUser, saveAuthSession } from "../../auth";

function mockJsonResponse(data) {
  return Promise.resolve({
    ok: true,
    json: async () => data,
  });
}

function buildPrescriptionRecord(overrides = {}) {
  return {
    id: 1,
    prescriptionNo: "RX-001",
    patientName: "张三",
    doctorId: 100,
    doctorName: "王医生",
    createdByRole: "DOCTOR",
    status: "DRAFT",
    doctorApprovalStatus: "NONE",
    items: [],
    ...overrides,
  };
}

function buildPrescriptionDetail(overrides = {}) {
  return {
    id: 1,
    prescriptionNo: "RX-001",
    patientName: "张三",
    doctorId: 100,
    doctorName: "王医生",
    createdByRole: "DOCTOR",
    status: "DRAFT",
    doctorApprovalStatus: "NONE",
    items: [
      {
        id: 11,
        drugId: 1,
        drugCode: "DRUG-001",
        drugName: "阿莫西林胶囊",
        dosage: "1粒",
        frequency: "bid",
        days: 3,
        quantity: 6,
      },
    ],
    ...overrides,
  };
}

describe("PrescriptionPage", () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    clearCurrentUser();
  });

  it("creates doctor prescription with current user payload", async () => {
    saveAuthSession({
      token: "doctor-token",
      user: { userId: 100, userName: "王医生", role: "DOCTOR" },
    });
    const user = userEvent.setup();

    global.fetch = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [buildPrescriptionRecord()],
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
                id: 1,
                drugCode: "DRUG-001",
                drugName: "阿莫西林胶囊",
              },
            ],
            total: 1,
            pageNum: 1,
            pageSize: 100,
          },
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: buildPrescriptionDetail(),
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: 9,
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [buildPrescriptionRecord({ id: 9, prescriptionNo: "RX-009" })],
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
          data: buildPrescriptionDetail({
            id: 9,
            prescriptionNo: "RX-009",
          }),
        })
      );

    render(<PrescriptionPage />);

    const createForm = screen.getByRole("heading", { name: "医生直接建方" }).closest("form");
    const scoped = within(createForm);

    await user.type(scoped.getByLabelText("患者姓名"), "李四");
    await user.selectOptions(scoped.getByLabelText("药品"), "1");
    await user.type(scoped.getByLabelText("剂量"), "2粒");
    await user.type(scoped.getByLabelText("频次"), "tid");
    await user.clear(scoped.getByLabelText("天数"));
    await user.type(scoped.getByLabelText("天数"), "5");
    await user.clear(scoped.getByLabelText("数量"));
    await user.type(scoped.getByLabelText("数量"), "10");
    await user.click(scoped.getByRole("button", { name: "创建处方" }));

    await waitFor(() => {
      expect(screen.getByText("处方创建成功，列表和详情已刷新。")).toBeInTheDocument();
    });

    expect(global.fetch.mock.calls[3][0]).toBe("http://localhost:8080/api/prescriptions");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"createdByRole\":\"DOCTOR\"");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"createdByUserId\":100");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"doctorId\":100");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"patientName\":\"李四\"");
  });

  it("shows pharmacist actions and sends audit payload", async () => {
    saveAuthSession({
      token: "pharmacist-token",
      user: { userId: 200, userName: "张药师", role: "PHARMACIST" },
    });
    const user = userEvent.setup();

    global.fetch = vi
      .fn()
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              buildPrescriptionRecord({
                id: 2,
                prescriptionNo: "RX-002",
                createdByRole: "PHARMACIST",
                status: "SUBMITTED",
                doctorApprovalStatus: "APPROVED",
              }),
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
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 100,
          },
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: buildPrescriptionDetail({
            id: 2,
            prescriptionNo: "RX-002",
            createdByRole: "PHARMACIST",
            status: "SUBMITTED",
            doctorApprovalStatus: "APPROVED",
          }),
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: null,
        })
      )
      .mockImplementationOnce(() =>
        mockJsonResponse({
          code: 0,
          message: "success",
          data: {
            records: [
              buildPrescriptionRecord({
                id: 2,
                prescriptionNo: "RX-002",
                createdByRole: "PHARMACIST",
                status: "APPROVED",
                doctorApprovalStatus: "APPROVED",
              }),
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
          data: buildPrescriptionDetail({
            id: 2,
            prescriptionNo: "RX-002",
            createdByRole: "PHARMACIST",
            status: "APPROVED",
            doctorApprovalStatus: "APPROVED",
            auditBy: "张药师",
          }),
        })
      );

    render(<PrescriptionPage />);

    expect(await screen.findByText("处方详情")).toBeInTheDocument();
    const actionCard = await screen.findByRole("button", { name: "药师审核通过" });
    await user.click(actionCard);

    await waitFor(() => {
      expect(screen.getByText("处方状态已更新，详情已刷新。")).toBeInTheDocument();
    });

    expect(global.fetch.mock.calls[3][0]).toBe("http://localhost:8080/api/prescriptions/2/audit");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"action\":\"APPROVE\"");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"operatorId\":200");
    expect(global.fetch.mock.calls[3][1].body).toContain("\"operatorName\":\"张药师\"");
  });
});
