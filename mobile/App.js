import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Modal,
  SafeAreaView,
  StatusBar,
  ActivityIndicator,
  Alert
} from 'react-native';

const API_BASE_URL = 'http://10.0.2.2:8080'; // 10.0.2.2 for Android Emulator, localhost for iOS simulator

export default function App() {
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [household, setHousehold] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalVisible, setModalVisible] = useState(false);
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('GROCERIES');
  const [splitType, setSplitType] = useState('EQUAL');

  useEffect(() => {
    // Initial dev login for testing
    handleDevLogin('junior@example.com', 'Junior');
  }, []);

  const handleDevLogin = async (email, name) => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/dev-login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, name })
      });
      const data = await res.json();
      setToken(data.token);
      setUser(data.user);
      setHousehold(data.household);
      fetchDashboard(data.token);
    } catch (err) {
      console.error(err);
      Alert.alert('Connection Error', 'Could not connect to backend server. Make sure the Spring Boot server is running.');
      setLoading(false);
    }
  };

  const fetchDashboard = async (authToken) => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/dashboard/summary`, {
        headers: { Authorization: `Bearer ${authToken || token}` }
      });
      const data = await res.json();
      setDashboard(data);
    } catch (err) {
      console.error('Error fetching dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddExpense = async () => {
    if (!title || !amount) {
      Alert.alert('Validation', 'Please provide title and amount');
      return;
    }
    try {
      const res = await fetch(`${API_BASE_URL}/api/expenses`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          title,
          amount: parseFloat(amount),
          category,
          splitType,
          expenseDate: new Date().toISOString().split('T')[0]
        })
      });
      if (res.ok) {
        setModalVisible(false);
        setTitle('');
        setAmount('');
        fetchDashboard(token);
        Alert.alert('Success', 'Expense recorded!');
      }
    } catch (err) {
      Alert.alert('Error', err.message);
    }
  };

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#4F46E5" />
        <Text style={{ marginTop: 12, color: '#64748B' }}>Connecting to CoupleFinance...</Text>
      </View>
    );
  }

  const b = dashboard?.balanceSummary;

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerTitle}>💑 {household?.name || 'Our Cozy Home'}</Text>
          <Text style={styles.headerSubtitle}>Active: {user?.name}</Text>
        </View>
        <TouchableOpacity
          onPress={() => handleDevLogin(user?.email === 'junior@example.com' ? 'ana@example.com' : 'junior@example.com', user?.name === 'Junior' ? 'Ana' : 'Junior')}
          style={styles.switchButton}>
          <Text style={styles.switchButtonText}>🔄 Switch Partner</Text>
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Balance Card */}
        <View style={styles.balanceCard}>
          <Text style={styles.balanceCardLabel}>COUPLE BALANCE</Text>
          <Text style={styles.balanceSummaryText}>
            {b?.isSettled ? '🎉 All Settled Up!' : b?.summaryMessage}
          </Text>
          <View style={styles.balanceRow}>
            <Text style={styles.balanceSpendLabel}>Total Spent This Month:</Text>
            <Text style={styles.balanceSpendValue}>${dashboard?.totalHouseholdSpending?.toFixed(2)}</Text>
          </View>
        </View>

        {/* Categories */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>SPENDING BY CATEGORY</Text>
          {dashboard?.categoryBreakdown?.map((cat) => (
            <View key={cat.category} style={styles.categoryRow}>
              <Text style={styles.categoryText}>{cat.icon} {cat.displayName}</Text>
              <Text style={styles.categoryAmount}>${cat.totalAmount?.toFixed(2)} ({cat.percentage}%)</Text>
            </View>
          ))}
        </View>

        {/* Recent Transactions */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>RECENT TRANSACTIONS</Text>
          {dashboard?.recentExpenses?.map((item) => (
            <View key={item.id} style={styles.expenseRow}>
              <View>
                <Text style={styles.expenseTitle}>{item.categoryIcon} {item.title}</Text>
                <Text style={styles.expenseSub}>Paid by {item.payer?.name} • {item.splitTypeDescription}</Text>
              </View>
              <Text style={styles.expenseAmount}>${item.amount?.toFixed(2)}</Text>
            </View>
          ))}
        </View>
      </ScrollView>

      {/* Floating Add Expense Button */}
      <TouchableOpacity style={styles.fab} onPress={() => setModalVisible(true)}>
        <Text style={styles.fabText}>＋ Add Expense</Text>
      </TouchableOpacity>

      {/* Add Expense Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Add Couple Expense</Text>

            <TextInput
              style={styles.input}
              placeholder="Title (e.g. Supermarket, Dinner)"
              value={title}
              onChangeText={setTitle}
            />

            <TextInput
              style={styles.input}
              placeholder="Amount ($)"
              keyboardType="decimal-pad"
              value={amount}
              onChangeText={setAmount}
            />

            <View style={styles.splitRow}>
              <TouchableOpacity
                style={[styles.splitChoice, splitType === 'EQUAL' && styles.splitChoiceActive]}
                onPress={() => setSplitType('EQUAL')}>
                <Text style={[styles.splitText, splitType === 'EQUAL' && styles.splitTextActive]}>50/50 Split</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.splitChoice, splitType === 'FULL_PARTNER_OWES' && styles.splitChoiceActive]}
                onPress={() => setSplitType('FULL_PARTNER_OWES')}>
                <Text style={[styles.splitText, splitType === 'FULL_PARTNER_OWES' && styles.splitTextActive]}>For Partner 100%</Text>
              </TouchableOpacity>
            </View>

            <View style={styles.modalButtons}>
              <TouchableOpacity style={styles.cancelButton} onPress={() => setModalVisible(false)}>
                <Text style={styles.cancelText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.saveButton} onPress={handleAddExpense}>
                <Text style={styles.saveText}>Save</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8FAFC' },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0'
  },
  headerTitle: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  headerSubtitle: { fontSize: 12, color: '#64748B' },
  switchButton: { backgroundColor: '#EEF2FF', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 12 },
  switchButtonText: { fontSize: 11, fontWeight: 'bold', color: '#4F46E5' },
  scrollContent: { padding: 16, paddingBottom: 100 },
  balanceCard: {
    backgroundColor: '#4338CA',
    borderRadius: 24,
    padding: 20,
    marginBottom: 16,
    shadowColor: '#4338CA',
    shadowOpacity: 0.3,
    shadowRadius: 10,
    elevation: 4
  },
  balanceCardLabel: { color: '#C7D2FE', fontSize: 11, fontWeight: '800', letterSpacing: 1 },
  balanceSummaryText: { color: '#FFF', fontSize: 18, fontWeight: 'bold', marginVertical: 8 },
  balanceRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8, borderTopWidth: 1, borderTopColor: 'rgba(255,255,255,0.2)', paddingTop: 8 },
  balanceSpendLabel: { color: '#E0E7FF', fontSize: 12 },
  balanceSpendValue: { color: '#FFF', fontSize: 14, fontWeight: 'bold' },
  section: { backgroundColor: '#FFF', borderRadius: 20, padding: 16, marginBottom: 16, borderWidth: 1, borderColor: '#E2E8F0' },
  sectionTitle: { fontSize: 11, fontWeight: '800', color: '#64748B', letterSpacing: 0.5, marginBottom: 12 },
  categoryRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 6 },
  categoryText: { fontSize: 13, color: '#334155', fontWeight: '500' },
  categoryAmount: { fontSize: 13, fontWeight: '700', color: '#0F172A' },
  expenseRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F1F5F9' },
  expenseTitle: { fontSize: 14, fontWeight: '600', color: '#0F172A' },
  expenseSub: { fontSize: 11, color: '#64748B', marginTop: 2 },
  expenseAmount: { fontSize: 14, fontWeight: '700', color: '#0F172A' },
  fab: {
    position: 'absolute',
    bottom: 24,
    left: 24,
    right: 24,
    backgroundColor: '#4F46E5',
    paddingVertical: 14,
    borderRadius: 20,
    alignItems: 'center',
    shadowColor: '#4F46E5',
    shadowOpacity: 0.4,
    shadowRadius: 8,
    elevation: 5
  },
  fabText: { color: '#FFF', fontSize: 15, fontWeight: 'bold' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  modalContent: { backgroundColor: '#FFF', borderTopLeftRadius: 28, borderTopRightRadius: 28, padding: 24 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', color: '#0F172A', marginBottom: 16 },
  input: { borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 14, padding: 12, marginBottom: 12, fontSize: 14 },
  splitRow: { flexDirection: 'row', gap: 8, marginBottom: 16 },
  splitChoice: { flex: 1, padding: 10, borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 12, alignItems: 'center' },
  splitChoiceActive: { backgroundColor: '#EEF2FF', borderColor: '#4F46E5' },
  splitText: { fontSize: 12, color: '#64748B', fontWeight: '600' },
  splitTextActive: { color: '#4F46E5', fontWeight: 'bold' },
  modalButtons: { flexDirection: 'row', gap: 12 },
  cancelButton: { flex: 1, padding: 12, alignItems: 'center' },
  cancelText: { color: '#64748B', fontWeight: '600' },
  saveButton: { flex: 1, backgroundColor: '#4F46E5', padding: 12, borderRadius: 14, alignItems: 'center' },
  saveText: { color: '#FFF', fontWeight: 'bold' }
});
