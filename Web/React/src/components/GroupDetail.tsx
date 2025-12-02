// src/components/GroupDetail.tsx
import React, { useEffect, useState } from "react";
import apiClient from "../services/api-client";
import { useAuth } from "../hooks/useAuth";
import "../App.css";
import { useCurrency } from "../contexts/CurrencyContext";

interface Expense {
  id: number;
  title: string;
  category: string;
  amount: number;
  userName: string;
  userInitial: string;
  date: string;
}

interface Props {
  groupId: number | null;
  onBack: () => void;
}

const GroupDetail: React.FC<Props> = ({ groupId, onBack }) => {
  const { user } = useAuth();
  const [groupName, setGroupName] = useState<string | null>(null);
  const [invitationCode, setInvitationCode] = useState<string | null>(null);
  const [showQR, setShowQR] = useState(false);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchGroupInfo = async () => {
    if (!groupId) return;

    try {
      const res = await apiClient.get(`/groups/${groupId}`);
      setGroupName(res.data.name || `Group ${groupId}`);
      setInvitationCode(res.data.invitation_code || null);
    } catch (err) {
      console.error("Failed to fetch group info", err);
      setGroupName(`Group ${groupId}`);
    }
  };

  const fetchGroupExpenses = async () => {
    if (!groupId) return;

    setLoading(true);
    try {
      const res = await apiClient.get(`/expenses/group/${groupId}`);
      const items = Array.isArray(res.data) ? res.data : [];

      // Get unique user IDs
      const uniqueUserIds = [...new Set(items.map((exp: any) => exp.user_id))];

      // Fetch user details for each unique user ID
      const userDetailsMap: Record<number, { first_name: string; last_name: string }> = {};
      await Promise.all(
        uniqueUserIds.map(async (userId: any) => {
          try {
            const userRes = await apiClient.get(`/users/${userId}`);
            userDetailsMap[userId] = {
              first_name: userRes.data.first_name || "User",
              last_name: userRes.data.last_name || "",
            };
          } catch (err) {
            console.error(`Failed to fetch user ${userId}`, err);
            userDetailsMap[userId] = { first_name: `User ${userId}`, last_name: "" };
          }
        })
      );

      const mapped: Expense[] = items.map((exp: any) => {
        const userDetails = userDetailsMap[exp.user_id] || {
          first_name: "Unknown",
          last_name: "",
        };
        const fullName = `${userDetails.first_name}`.trim();

        return {
          id: exp.id,
          title: exp.title || "Untitled",
          category: exp.category || "Other",
          amount: exp.amount || 0,
          userName: fullName,
          userInitial: (userDetails.first_name[0] || "U").toUpperCase(),
          date: exp.created_at
            ? new Date(exp.created_at).toISOString().slice(0, 10)
            : new Date().toISOString().slice(0, 10),
        };
      });

      setExpenses(mapped);
    } catch (err) {
      console.error("Failed to fetch group expenses", err);
      setExpenses([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!groupId) return;
    fetchGroupInfo();
    fetchGroupExpenses();
  }, [groupId]);

  const cur = useCurrency();

  return (
    <div>
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 12 }}>
        <div
          onClick={onBack}
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: 8,
            cursor: "pointer",
            color: "var(--purple-1)",
            fontWeight: 700,
          }}
          aria-label="Back to groups"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M15 18L9 12L15 6"
              stroke="currentColor"
              strokeWidth="1.6"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          Back
        </div>

        <div className="bp-title">{groupName ?? "Group"}</div>
      </div>

      <div className="bp-section-title" style={{ marginTop: 12, fontSize: 14 }}>
        Expenses for this group
      </div>

      <div style={{ marginTop: 12, display: "flex", flexDirection: "column", gap: 12 }}>
        {expenses.map((e) => (
          <article key={e.id} className="bp-tx">
            <div
              className="bp-thumb"
              style={{ width: 48, height: 48, borderRadius: 10, fontSize: 16 }}
            >
              {e.userInitial}
            </div>

            <div className="bp-meta">
              <div className="bp-tx-title">{e.title}</div>
              <div className="bp-tx-cat">
                {e.category} • {e.userName}
              </div>
              <div style={{ marginTop: 8, fontSize: 12, color: "var(--muted-dark)" }}>
                {e.date}
              </div>
            </div>

            <div
              style={{ marginLeft: 12, fontWeight: 800 }}
              className={`bp-amount ${e.amount < 0 ? "negative" : "positive"}`}
            >
              {e.amount < 0 ? "-" : "+"}
              {cur.formatAmount(Math.abs(e.amount))}
            </div>
          </article>
        ))}
      </div>

      {/* footer action — Add expense */}
      <div style={{ marginTop: 18, display: "flex", gap: 8 }}>
        <button
          className="bp-add-btn"
          onClick={async () => {
            if (!user?.id || !groupId) {
              alert("User or group not available");
              return;
            }

            const title = prompt("Expense title", "New Expense");
            if (!title) return;

            const amountRaw = prompt("Amount (positive number)", "10");
            const amount = amountRaw ? parseFloat(amountRaw) || 0 : 0;

            if (amount <= 0) {
              alert("Amount must be positive");
              return;
            }

            const category = prompt("Category", "Misc") || "Misc";

            try {
              const body = {
                title,
                category,
                amount,
                user_id: user.id,
                group_id: groupId,
              };

              await apiClient.post("/expenses", body);

              // Refresh the expense list
              await fetchGroupExpenses();

              alert("Expense added successfully!");
            } catch (err: any) {
              console.error("Failed to create expense", err);
              alert(`Failed to add expense: ${err.response?.data?.detail || err.message}`);
            }
          }}
          disabled={loading}
        >
          Add Expense
        </button>

        <button className="bp-add-btn" onClick={() => setShowQR(!showQR)}>
          {showQR ? "Hide QR Code" : "Show QR Code"}
        </button>
      </div>

      {/* QR Code Display */}
      {showQR && groupId && (
        <div
          style={{
            marginTop: 20,
            padding: 20,
            background: "var(--card-bg)",
            borderRadius: 12,
            border: "1px solid rgba(0,0,0,0.08)",
            textAlign: "center",
          }}
        >
          <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 10 }}>
            Group Invitation
          </div>

          {invitationCode && (
            <div
              style={{
                fontSize: 24,
                fontWeight: 800,
                color: "var(--purple-1)",
                marginBottom: 15,
                letterSpacing: 2,
              }}
            >
              {invitationCode}
            </div>
          )}

          <img
            src={`http://localhost:8000/groups/${groupId}/invite-qr`}
            alt="Group QR Code"
            style={{
              maxWidth: "100%",
              width: 250,
              height: 250,
              border: "2px solid var(--purple-1)",
              borderRadius: 8,
              padding: 10,
              background: "#fff",
            }}
            onError={(e) => {
              console.error("Failed to load QR code", e);
              alert("Failed to load QR code. Check console for details.");
            }}
          />

          <div style={{ marginTop: 10, color: "var(--muted-dark)", fontSize: 13 }}>
            Share this code or QR code to invite members
          </div>
        </div>
      )}
    </div>
  );
};

export default GroupDetail;
